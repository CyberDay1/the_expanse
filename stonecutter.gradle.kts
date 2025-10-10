import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import dev.kikugie.stonecutter.process.SCPrepareTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

private val STONECUTTER_TEMPLATE_INCLUDES = listOf(
    "**/*.java",
    "**/*.kt",
    "**/*.kts",
    "**/*.groovy",
    "**/*.gradle",
    "**/*.scala",
    "**/*.sc",
    "**/*.json",
    "**/*.json5",
    "**/*.hjson",
    "**/*.properties",
    "**/*.mcmeta",
    "**/*.toml"
)

private fun String.stonecutterSourceSet(prefix: String): String {
    val suffix = removePrefix(prefix)
    return if (suffix.isEmpty()) "main" else suffix.replaceFirstChar { it.lowercase() }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

private val DEFAULT_VARIANT = "1.21.1-neoforge"

val requestedActive = providers.gradleProperty("stonecutter.active").orElse(DEFAULT_VARIANT)

val stonecutter: StonecutterControllerExtension = extensions.getByType()

stonecutter active requestedActive

stonecutter.parameters {
    val propertiesFile = node.project.projectDir.resolve("gradle.properties")
    if (propertiesFile.isFile) {
        Properties().apply {
            propertiesFile.inputStream().use(::load)
        }.forEach { (key, value) ->
            node.project.extensions.extraProperties[key.toString()] = value
        }
    }

    val templateRoot = node.project.rootProject.layout.projectDirectory.dir("template/src")

    node.project.tasks.withType(SCPrepareTask::class.java).configureEach {
        val sourceSet = name.stonecutterSourceSet("stonecutterPrepare")
        val templateDir = templateRoot.dir(sourceSet)
        if (!templateDir.asFile.exists()) {
            return@configureEach
        }

        root.set(templateDir.asFile)
        source.setFrom(node.project.rootProject.fileTree(templateDir) {
            STONECUTTER_TEMPLATE_INCLUDES.forEach { include(it) }
        })
    }

    node.project.tasks.withType(Sync::class.java).configureEach {
        if (!name.startsWith("stonecutterGenerate")) return@configureEach

        val sourceSet = name.stonecutterSourceSet("stonecutterGenerate")
        val templateDir = templateRoot.dir(sourceSet)
        if (!templateDir.asFile.exists()) {
            return@configureEach
        }

        from(templateDir)
    }

    node.project.tasks.withType(Copy::class.java).configureEach {
        if (!name.startsWith("stonecutterMerge")) return@configureEach

        val sourceSet = name.stonecutterSourceSet("stonecutterMerge")
        val templateDir = templateRoot.dir(sourceSet)
        if (!templateDir.asFile.exists()) {
            return@configureEach
        }

        into(templateDir)
    }
}

tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds the Stonecutter variant selected via -Pstonecutter.active (defaults to $DEFAULT_VARIANT)."

    val variant = requestedActive.get()
    checkNotNull(stonecutter.versions.find { it.project == variant }) {
        "Unknown Stonecutter variant '$variant'."
    }

    dependsOn(":$variant:build")
}

tasks.register("checkAllVariants") {
    group = "verification"
    description = "Runs the `check` task for every configured Stonecutter variant."
    dependsOn(stonecutter.versions.map { ":${it.project}:check" })
}
