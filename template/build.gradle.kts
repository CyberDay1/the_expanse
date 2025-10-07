plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(
            project.findProperty("JAVA_VERSION")?.toString()?.toInt() ?: 21
        )
    }
    withSourcesJar()
}

group = project.findProperty("MOD_GROUP") as String? ?: "com.theexpanse"
version = project.findProperty("MOD_VERSION") as String? ?: "0.1.0"

base {
    archivesName.set(project.findProperty("MOD_NAME") as String? ?: "the_expanse")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.neoforged.net/releases") }
}

dependencies {
    implementation("net.neoforged:neoforge:${project.findProperty("NEOFORGE_VERSION")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("packFormat", project.findProperty("PACK_FORMAT") ?: "48")

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(
            mapOf(
                "version" to project.version,
                "packFormat" to (project.findProperty("PACK_FORMAT") ?: "48")
            )
        )
    }
}

tasks.jar {
    from("src/main/resources")
    manifest {
        attributes(
            "Specification-Title" to (project.findProperty("MOD_NAME") ?: "the_expanse"),
            "Specification-Vendor" to "CyberDay1",
            "Specification-Version" to project.version,
            "Implementation-Title" to (project.findProperty("MOD_NAME") ?: "the_expanse"),
            "Implementation-Version" to project.version,
        )
    }
}
