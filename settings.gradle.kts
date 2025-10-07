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
        id("dev.kayla.stonecutter") version "0.7.10"
    }
}
plugins {
    id("dev.kayla.stonecutter") version "0.7.10"
}
rootProject.name = "the_expanse"
stonecutter {
    shared {
        set("JAVA_VERSION", "21")
    }
    kotlin.set(false)
    create("1.21.1-neoforge") {
        set("MC_VERSION", "1.21.1")
        set("NEOFORGE_VERSION", "21.1.209") // override in gradle.properties if needed
    }
    create("1.21.4-neoforge") {
        set("MC_VERSION", "1.21.4")
        set("NEOFORGE_VERSION", "21.4.154")
    }
}
include(":1.21.1-neoforge")
include(":1.21.4-neoforge")

