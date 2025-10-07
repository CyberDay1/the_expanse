plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

group = "com.theexpanse"
version = "0.1.0"
base.archivesName.set("the_expanse")

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation("net.neoforged:neoforge:${project.loaderVersion}")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from("src/main/resources")
    manifest {
        attributes(
            "Specification-Title" to "the_expanse",
            "Implementation-Title" to "the_expanse",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "CyberDay1"
        )
    }
}

tasks.named("build") {
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
