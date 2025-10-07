import org.gradle.api.Project
import java.util.Properties

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

val configurableProperties = Properties().apply {
    val configFile = rootProject.file("configurable.properties")
    if (configFile.exists()) {
        configFile.inputStream().use { load(it) }
    }
}

fun Project.resolveToggle(key: String, default: Boolean): Boolean {
    val cliOverride = findProperty(key)?.toString()?.lowercase()
    val fileValue = configurableProperties.getProperty(key)?.lowercase()
    val resolved = cliOverride ?: fileValue
    return resolved?.let { it == "true" } ?: default
}

val enableDatagen = project.resolveToggle("enableDatagen", true)
val useMixins = project.resolveToggle("useMixins", false)

extensions.extraProperties["enableDatagen"] = enableDatagen
extensions.extraProperties["useMixins"] = useMixins

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

if (!enableDatagen) {
    tasks.configureEach {
        if (name.contains("datagen", ignoreCase = true)) {
            enabled = false
        }
    }
}

if (!useMixins) {
    tasks.configureEach {
        if (name.contains("mixin", ignoreCase = true)) {
            enabled = false
        }
    }
}
