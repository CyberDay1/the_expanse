import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import java.util.Properties

private val stonecutterConfigFile: File = settings.settingsDir.resolve("stonecutter.json")
private val versionsDirectory: File = settings.settingsDir.resolve("versions")
private val NEOFORGE_BUILD_SCRIPT = "build.neoforge.gradle.kts"
private val DEFAULT_VARIANT = "1.21.1-neoforge"
private val SUPPORTED_VARIANTS = listOf(
    "1.21.1-neoforge",
    "1.21.2-neoforge",
    "1.21.3-neoforge",
    "1.21.4-neoforge",
    "1.21.5-neoforge",
    "1.21.6-neoforge",
    "1.21.7-neoforge",
    "1.21.8-neoforge",
    "1.21.9-neoforge",
)

@Suppress("UNCHECKED_CAST")
private fun loadStonecutterConfig(): MutableMap<String, Any?> {
    if (!stonecutterConfigFile.exists()) {
        return linkedMapOf("variants" to linkedMapOf<String, Any?>())
    }
    val parsed = JsonSlurper().parse(stonecutterConfigFile) as? Map<String, Any?> ?: emptyMap()
    val root = LinkedHashMap(parsed)
    val variants = (parsed["variants"] as? Map<String, Any?>)?.let { LinkedHashMap(it) }
        ?: linkedMapOf<String, Any?>()
    root["variants"] = variants
    return root
}

private fun saveStonecutterConfig(config: Map<String, Any?>) {
    stonecutterConfigFile.parentFile.mkdirs()
    val json = JsonOutput.prettyPrint(JsonOutput.toJson(config))
    stonecutterConfigFile.writeText(json + "\n")
}

private fun updateStonecutterConfig(block: MutableMap<String, Any?>.() -> Unit) {
    val config = loadStonecutterConfig()
    config.block()
    saveStonecutterConfig(config)
}

private fun loadVariantProperties(variant: String): Map<String, String> {
    val propertiesFile = versionsDirectory.resolve("$variant/gradle.properties")
    require(propertiesFile.isFile) { "Missing gradle.properties for variant '$variant'" }

    val properties = Properties()
    propertiesFile.inputStream().use { properties.load(it) }

    return properties.entries.associate { (key, value) ->
        key.toString() to value.toString()
    }
}

private class StonecutterVersionDsl {
    val extra: MutableMap<String, Any?> = linkedMapOf()
    var buildscript: String? = null
}

private class StonecutterVersionsDsl {
    private val versions: MutableMap<String, Map<String, Any?>> = linkedMapOf()

    fun register(name: String, block: StonecutterVersionDsl.() -> Unit) {
        val dsl = StonecutterVersionDsl().apply(block)
        val entry = LinkedHashMap<String, Any?>()
        entry["replace"] = LinkedHashMap(dsl.extra)
        dsl.buildscript?.let { entry["buildscript"] = it }
        versions[name] = entry
    }

    fun asMap(): Map<String, Map<String, Any?>> = LinkedHashMap(versions)
}

private fun StonecutterSettingsExtension.active(version: String) {
    updateStonecutterConfig { this["default"] = version }
}

private fun StonecutterSettingsExtension.versions(block: StonecutterVersionsDsl.() -> Unit) {
    val variants = StonecutterVersionsDsl().apply(block).asMap()
    updateStonecutterConfig { this["variants"] = LinkedHashMap(variants) }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        mavenCentral()
    }
    plugins {
        id("dev.kikugie.stonecutter") version "0.7.10"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "the_expanse"

stonecutter {
    shared {
        centralScript.set("stonecutter.gradle.kts")
    }
    active(DEFAULT_VARIANT)

    versions {
        SUPPORTED_VARIANTS.forEach { variant ->
            register(variant) {
                buildscript = NEOFORGE_BUILD_SCRIPT
                loadVariantProperties(variant).forEach { (key, value) ->
                    extra[key] = value
                }
            }
        }
    }
}
