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

rootProject.name = "the_expanse"

apply(from = "stonecutter.gradle.kts")
