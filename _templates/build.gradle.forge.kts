plugins {
    id("java")
    id("base") // clean, assemble
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("maven-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
    maven("https://maven.minecraftforge.net")
}

minecraft {
    mappings("official", project.mcVersion as String)
}

dependencies {
    minecraft("net.minecraftforge:forge:-")
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

tasks.register("buildMod") {
    group = "build"
    description = "Build distributable mod jar"
    dependsOn("jar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])
        }
    }
}
