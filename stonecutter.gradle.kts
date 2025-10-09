import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

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
