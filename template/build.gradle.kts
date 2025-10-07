plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

val modGroup = (project.findProperty("MOD_GROUP") as String?) ?: "com.theexpanse"
val modVersion = (project.findProperty("MOD_VERSION") as String?) ?: "0.1.0"
val modName = (project.findProperty("MOD_NAME") as String?) ?: "the_expanse"
val neoForgeVersion = (project.findProperty("NEOFORGE_VERSION") as String?) ?: "21.1.209"

group = modGroup
version = modVersion
base {
    archivesName.set(modName)
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.neoforged.net/releases") }
    maven { url = uri("https://maven.kikugie.dev/releases") } // Stonecutter deps
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(mapOf("version" to project.version))
    }
}

tasks.jar {
    from("src/main/resources")
    manifest {
        attributes(
            "Specification-Title" to "the_expanse",
            "Specification-Vendor" to "CyberDay1",
            "Specification-Version" to project.version,
            "Implementation-Title" to "the_expanse",
            "Implementation-Version" to project.version,
        )
    }
}

