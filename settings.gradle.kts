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
    vcsVersion = "1.21.4"
    defaultVersion = "1.21.4"
    controller("neoforge", "stonecutter.gradle.kts")
    versions {
        "1.21.1"()
        "1.21.2"()
        "1.21.3"()
        "1.21.4"()
        "1.21.5"()
        "1.21.6"()
        "1.21.7"()
        "1.21.8"()
        "1.21.9"()
    }
}
