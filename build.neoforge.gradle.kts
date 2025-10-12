plugins {
    id("net.neoforged.gradle") version "6.0.18"
    id("java")
    idea
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

val mcVersion: String by extra
val neoforgeVersion: String by extra
val packFormat: String by extra

minecraft {
    version(mcVersion)
}

dependencies {
    // Pull version from stonecutter.json vars["NEOFORGE"]
    implementation("net.neoforged:neoforge:$neoforgeVersion")
}

tasks.withType<Jar> {
    archiveBaseName.set("the_expanse")
    archiveVersion.set(mcVersion)
    manifest {
        attributes(
            "Specification-Title" to "The Expanse",
            "Specification-Vendor" to "YourNameOrTeam",
            "Implementation-Title" to "The Expanse Mod",
            "Implementation-Version" to archiveVersion.get(),
            "Implementation-Vendor" to "YourNameOrTeam"
        )
    }
}

tasks.register("printVersions") {
    group = "help"
    description = "Prints currently active Minecraft and NeoForge versions."
    doLast {
        println("Minecraft version: $mcVersion")
        println("NeoForge version: $neoforgeVersion")
        println("Pack format: $packFormat")
    }
}
