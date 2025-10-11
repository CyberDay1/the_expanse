plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
    id("net.neoforged.gradle") version "6.0.18" apply false
    idea
}

allprojects {
    repositories {
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}
