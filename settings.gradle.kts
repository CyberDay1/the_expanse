pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "the_expanse"

// Load Stonecutter matrix configuration
apply(from = "stonecutter.gradle.kts")
