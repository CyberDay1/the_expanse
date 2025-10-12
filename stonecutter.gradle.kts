// stonecutter.gradle.kts
// Controller script for Stonecutter 0.7.10
// This script runs once per registered version

plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.neoforged.net/snapshots")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

base {
    archivesName.set("TheExpanse-NeoForge")
}

group = "com.theexpanse"
version = rootProject.property("MOD_VERSION") ?: "1.0.0"
