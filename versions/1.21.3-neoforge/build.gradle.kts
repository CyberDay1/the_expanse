plugins {
    id("java")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version "7.0.190" apply false
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
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.minecraftforge.net")
}

dependencies {
    if (project.hasProperty("loader") && project.property("loader") == "forge") {
        "minecraft"("net.minecraftforge:forge:${project.property("mcVersion")}-${project.property("loaderVersion")}")
    } else {
        implementation("net.neoforged:neoforge:${project.property("loaderVersion")}")
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
            "Implementation-Vendor" to "CyberDay1"
        )
    }
}

tasks.register("buildMod") {
    group = "build"
    description = "Builds the distributable mod jar"
    dependsOn(tasks.jar, tasks.named("sourcesJar"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])
        }
    }
}
