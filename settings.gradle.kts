pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "expanse_heights"

stonecutter {
    create(rootProject.file("stonecutter.versions.json"))
    // You can still enable a default version here if needed:
    // enable("1.21.1-neoforge")
}
