import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import java.util.Properties

private val configurableProperties = Properties().apply {
    val configFile = rootProject.file("configurable.properties")
    if (configFile.exists()) {
        configFile.inputStream().use { load(it) }
    }
}

private fun Project.resolveToggle(key: String, default: Boolean): Boolean {
    val cliOverride = findProperty(key)?.toString()?.lowercase()
    val fileValue = configurableProperties.getProperty(key)?.lowercase()
    val resolved = cliOverride ?: fileValue
    return resolved?.let { it == "true" } ?: default
}

private fun Project.loadStonecutterDefault(): String? {
    val configFile = rootProject.file("stonecutter.json")
    if (!configFile.exists()) return null

    val parsed = JsonSlurper().parse(configFile)
    return (parsed as? Map<*, *>)?.get("default")?.toString()
}

private fun Project.discoverFirstVariant(): String? {
    val versionsDir = rootProject.layout.projectDirectory.dir("versions").asFile
    if (!versionsDir.isDirectory) return null

    return versionsDir.listFiles()
        ?.filter { it.isDirectory }
        ?.sortedBy { it.name }
        ?.firstOrNull()
        ?.name
}

private fun Project.resolveActiveVariant(): String {
    val propertyVariant = listOf(
        providers.gradleProperty("stonecutter.active"),
        providers.environmentVariable("STONECUTTER_ACTIVE"),
        providers.systemProperty("stonecutter.active")
    ).asSequence()
        .mapNotNull { it.orNull?.takeIf(String::isNotBlank) }
        .firstOrNull()

    if (propertyVariant != null) {
        return propertyVariant
    }

    return loadStonecutterDefault()
        ?: discoverFirstVariant()
        ?: error("Unable to resolve active Stonecutter variant. Provide -Pstonecutter.active or ensure stonecutter.json defines a default variant.")
}

val enableDatagen = project.resolveToggle("enableDatagen", true)

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

val activeVariant = project.resolveActiveVariant()

extensions.configure<SourceSetContainer>("sourceSets") {
    named("main") {
        val variantSourceRoot = project.layout.projectDirectory.dir("versions/$activeVariant/src")

        val variantJava = variantSourceRoot.dir("main/java").asFile
        if (variantJava.isDirectory) {
            java.srcDir(variantJava)
        }

        val variantResources = variantSourceRoot.dir("main/resources").asFile
        if (variantResources.isDirectory) {
            resources.srcDir(variantResources)
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
