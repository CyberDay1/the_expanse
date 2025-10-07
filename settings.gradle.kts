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

// Register subprojects from the version descriptor
stonecutter {
    create(rootProject, file("stonecutter.versions.json"))
}
