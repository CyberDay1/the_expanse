plugins {
    id("java")
    id("base") // clean, assemble
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

group = "com.theexpanse"
version = project.findProperty("modVersion") ?: "0.1.0"

base {
    archivesName.set("the_expanse")
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    val neoVersion = project.findProperty("loaderVersion")?.toString() ?: "21.1.+"
    implementation("net.neoforged:neoforge:$neoVersion")
}

tasks.jar {
    from("src/main/resources")
    archiveBaseName.set("the_expanse")
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

// reobfJar is optional — only register if NeoForge plugin doesn’t already
val reobfJar = tasks.findByName("reobfJar") ?: tasks.register("reobfJar") {
    dependsOn(tasks.jar)
}

/**
 * Explicit buildMod task.
 * Always registered so it shows in `gradlew tasks`.
 */
tasks.register("buildMod") {
    group = "build"
    description = "Build distributable mod jar"
    dependsOn(tasks.named("jar"), reobfJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])
        }
    }
}
