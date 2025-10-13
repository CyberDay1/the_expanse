plugins {
    id("base")
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
    description = "Builds all version variants."
    subprojects.forEach { dependsOn("${it.path}:build") }
}

tasks.register("cleanAll") {
    group = "build"
    description = "Cleans all version variants."
    subprojects.forEach { dependsOn("${it.path}:clean") }
}
