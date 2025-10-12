pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.neoforged.net/snapshots")
        maven("https://maven.teamresourceful.com/repository/maven-public/")
        maven("https://maven.resourcefulbees.com/repository/maven-public/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    create(rootProject)
}

rootProject.name = "the_expanse"
