pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "the_expanse"

stonecutter {
    vcs = "1.21.4"
    defaultVersion = "1.21.1"
    controller = "stonecutter.gradle.kts"
}
