import dev.kikugie.stonecutter.settings.StonecutterSettingsExtension
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File

private val stonecutterConfigFile: File = settings.settingsDir.resolve("stonecutter.json")

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

private class StonecutterVersionDsl {
    val extra: MutableMap<String, Any?> = linkedMapOf()
}

private class StonecutterVersionsDsl {
    private val versions: MutableMap<String, Map<String, Any?>> = linkedMapOf()

    fun register(name: String, block: StonecutterVersionDsl.() -> Unit) {
        val dsl = StonecutterVersionDsl().apply(block)
        versions[name] = linkedMapOf("replace" to LinkedHashMap(dsl.extra))
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
    active("1.21.1-neoforge")

    versions {
        register("1.21.1-neoforge") {
            extra["MC_VERSION"] = "1.21.1"
            extra["NEOFORGE_VERSION"] = "21.1.209"
            extra["PACK_FORMAT"] = "48"
        }
        register("1.21.2-neoforge") {
            extra["MC_VERSION"] = "1.21.2"
            extra["NEOFORGE_VERSION"] = "21.2.84"
            extra["PACK_FORMAT"] = "57"
        }
        register("1.21.3-neoforge") {
            extra["MC_VERSION"] = "1.21.3"
            extra["NEOFORGE_VERSION"] = "21.3.64"
            extra["PACK_FORMAT"] = "57"
        }
        register("1.21.4-neoforge") {
            extra["MC_VERSION"] = "1.21.4"
            extra["NEOFORGE_VERSION"] = "21.4.154"
            extra["PACK_FORMAT"] = "61"
        }
        register("1.21.5-neoforge") {
            extra["MC_VERSION"] = "1.21.5"
            extra["NEOFORGE_VERSION"] = "21.5.72"
            extra["PACK_FORMAT"] = "71"
        }
        register("1.21.6-neoforge") {
            extra["MC_VERSION"] = "1.21.6"
            extra["NEOFORGE_VERSION"] = "21.6.43"
            extra["PACK_FORMAT"] = "80"
        }
        register("1.21.7-neoforge") {
            extra["MC_VERSION"] = "1.21.7"
            extra["NEOFORGE_VERSION"] = "21.7.12"
            extra["PACK_FORMAT"] = "81"
        }
        register("1.21.8-neoforge") {
            extra["MC_VERSION"] = "1.21.8"
            extra["NEOFORGE_VERSION"] = "21.8.17"
            extra["PACK_FORMAT"] = "81"
        }
        register("1.21.9-neoforge") {
            extra["MC_VERSION"] = "1.21.9"
            extra["NEOFORGE_VERSION"] = "21.9.6"
            extra["PACK_FORMAT"] = "88"
        }
    }
}
