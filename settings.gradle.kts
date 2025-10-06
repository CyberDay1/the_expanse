pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases")   // <-- add this
        maven("https://maven.kikugie.dev/releases")
    }
}


plugins {
    id("dev.kikugie.stonecutter") version "0.6.2"
}

rootProject.name = "expanse_heights"
