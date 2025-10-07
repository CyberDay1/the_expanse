plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation("net.neoforged:neoforge:${property("NEOFORGE_VERSION")}")
}

// Expand tokens in resources (mods.toml, pack.mcmeta, etc.)
tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcVersion", property("MC_VERSION"))
    inputs.property("neoVersion", property("NEOFORGE_VERSION"))

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version,
            "mcVersion" to property("MC_VERSION"),
            "neoVersion" to property("NEOFORGE_VERSION")
        )
    }

    filesMatching("pack.mcmeta") {
        expand(
            "version" to project.version,
            "mcVersion" to property("MC_VERSION"),
            "neoVersion" to property("NEOFORGE_VERSION"),
            "packFormat" to property("PACK_FORMAT")
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
        }
    }
}
