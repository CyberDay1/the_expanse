pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.neoforged.net/snapshots")
        maven("https://maven.teamresourceful.com/repository/maven-public/")
        maven("https://maven.resourcefulbees.com/repository/maven-public/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "the_expanse"

stonecutter {
    create(rootProject) {
        kotlinController.set(true)
        centralScript.set("build.neoforge.gradle.kts")
        vcsVersion.set("1.21.1")
        versions(
            "1.21.1",
            "1.21.2",
            "1.21.3",
            "1.21.4",
            "1.21.5",
            "1.21.6",
            "1.21.7",
            "1.21.8",
            "1.21.9",
            "1.21.10"
        )
    }
}
