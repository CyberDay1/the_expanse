import org.gradle.api.tasks.JavaExec
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


val datapackValidationTest by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Validates Patchouli cross-link metadata for JEI HUD overlays."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.theexpanse.datapack.DatapackValidationTest")
    workingDir = project.projectDir
}

tasks.named("check") {
    dependsOn(datapackValidationTest)
}
