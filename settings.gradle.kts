import org.gradle.api.Action
import org.gradle.api.artifacts.verification.DependencyVerificationMode
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.dependencyLocking

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        mavenCentral()
    }
    plugins {
        id("dev.kikugie.stonecutter") version "0.7.10"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

class DependencyVerificationSpec(private val settings: Settings) {
    fun verify() {
        val dependencyVerificationMethod = settings::class.java.methods.firstOrNull { method ->
            method.name == "dependencyVerification" && method.parameterTypes.singleOrNull() == Action::class.java
        }
        if (dependencyVerificationMethod != null) {
            val enableVerificationAction = object : Action<Any> {
                override fun execute(target: Any) {
                    val verifyMethod = target::class.java.methods.firstOrNull { method ->
                        method.name == "verify" && method.parameterCount == 0
                    }
                    if (verifyMethod != null) {
                        verifyMethod.invoke(target)
                    } else {
                        settings.gradle.startParameter.dependencyVerificationMode = DependencyVerificationMode.STRICT
                    }
                }
            }
            dependencyVerificationMethod.invoke(settings, enableVerificationAction)
        } else {
            settings.gradle.startParameter.dependencyVerificationMode = DependencyVerificationMode.STRICT
        }
    }
}

fun Settings.dependencyVerification(configuration: DependencyVerificationSpec.() -> Unit) {
    DependencyVerificationSpec(this).apply(configuration)
}

dependencyVerification {
    verify()
}

gradle.settingsEvaluated {
    if (System.getenv("CI")?.equals("true", ignoreCase = true) == true) {
        val startParameters = gradle.startParameter
        if (startParameters.isWriteDependencyLocks || startParameters.lockedDependenciesToUpdate.isNotEmpty()) {
            error("Dependency lock updates are not permitted in CI runs.")
        }
    }
    gradle.rootProject {
        dependencyLocking {
            lockAllConfigurations()
        }
    }
}

rootProject.name = "the_expanse"

val supportedVariants = listOf(
    "1.21.1-neoforge",
    "1.21.2-neoforge",
    "1.21.3-neoforge",
    "1.21.4-neoforge",
    "1.21.5-neoforge",
    "1.21.6-neoforge",
    "1.21.7-neoforge",
    "1.21.8-neoforge",
    "1.21.9-neoforge",
)

val defaultVariant = "1.21.1-neoforge"
val requestedVariant = providers.gradleProperty("stonecutter.active")
    .orElse(defaultVariant)
    .get()

require(requestedVariant in supportedVariants) {
    "Unknown Stonecutter variant '$requestedVariant'. Supported variants: ${supportedVariants.joinToString()}"
}

val activeVariants = linkedSetOf(defaultVariant, requestedVariant)

stonecutter {
    create(rootProject) {
        kotlinController.set(true)
        centralScript.set("build.neoforge.gradle.kts")
        vcsVersion.set(defaultVariant)

        versions(activeVariants)
    }
}
