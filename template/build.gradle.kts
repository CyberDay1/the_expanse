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

group = "com.theexpanse"
version = "0.1.0"
base {
    archivesName.set("the_expanse")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.neoforged.net/releases") }
    maven { url = uri("https://maven.kikugie.dev/releases") } // Stonecutter deps
}

dependencies {
    implementation("net.neoforged:neoforge:${project.findProperty("NEOFORGE_VERSION") ?: "21.1.209"}")
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

