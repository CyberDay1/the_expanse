plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
        import dev.kikugie.stonecutter.process.SCPrepareTask
        import org.gradle.api.tasks.Copy
        import org.gradle.api.tasks.Sync
        import org.gradle.api.tasks.bundling.Jar
        import org.gradle.kotlin.dsl.getByType

        private val DEFAULT_VARIANT = "1.21.1"

val stonecutter = extensions.getByType<StonecutterControllerExtension>()
stonecutter active providers.gradleProperty("stonecutter.active").orElse(DEFAULT_VARIANT)

tasks.register("chiseledBuild") {
    group = "build"
    description = "Builds the currently active Stonecutter variant."
    dependsOn("build")
}

tasks.register("checkAllVariants") {
    group = "verification"
    description = "Runs the check task for every configured Stonecutter variant."
    dependsOn(stonecutter.versions.map { ":${it.project}:check" })
}
