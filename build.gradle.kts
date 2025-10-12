plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.200"
    id("maven-publish")
    id("base")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.neoforged.net/snapshots")
        maven("https://libraries.minecraft.net")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://jitpack.io")
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds all Stonecutter version variants."
    dependsOn(subprojects.map { "${it.path}:build" })
}

tasks.register("cleanAll") {
    group = "build"
    description = "Cleans all Stonecutter version variants."
    dependsOn(subprojects.map { "${it.path}:clean" })
}

// Optional: make the root build depend on all subprojects
tasks.named("build") {
    dependsOn("buildAll")
}
