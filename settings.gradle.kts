pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("net.neoforged.moddev") version "1.+"
}

rootProject.name = "the_expanse"
