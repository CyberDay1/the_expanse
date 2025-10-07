import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension
import java.io.File

fun StonecutterSettingsExtension.versions(path: File) {
    create(rootProject, path)
}

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
    versions(file("stonecutter.versions.json"))
}
