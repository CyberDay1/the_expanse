import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.util.Properties

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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

val mcVersion = project.property("MC_VERSION").toString()
val neoForgeVersion = project.property("NEOFORGE_VERSION").toString()
val packFormat = project.property("PACK_FORMAT").toString()

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
}

val sourceSets = extensions.getByType<SourceSetContainer>()

val datapackValidationTest = tasks.register<JavaExec>("datapackValidationTest") {
    group = "verification"
    description = "Validates Patchouli cross-link metadata for JEI HUD overlays."
    classpath = sourceSets.named("test").get().runtimeClasspath
    mainClass.set("com.theexpanse.datapack.DatapackValidationTest")
    workingDir = projectDir
    dependsOn(tasks.named("testClasses"))
}

tasks.matching { it.name == "check" }.configureEach {
    dependsOn(datapackValidationTest)
}

val ciBuild = providers.environmentVariable("CI").map { it.equals("true", ignoreCase = true) }.orElse(false)

tasks.withType<JavaCompile>().configureEach {
    if (ciBuild.orNull == true && !options.compilerArgs.contains("-Werror")) {
        options.compilerArgs.add("-Werror")
    }
}

// Expand tokens in resources (mods.toml, pack.mcmeta, etc.)
tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcVersion", mcVersion)
    inputs.property("neoVersion", neoForgeVersion)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version,
            "mcVersion" to mcVersion,
            "neoVersion" to neoForgeVersion
        )
    }

    filesMatching("pack.mcmeta") {
        expand(
            "version" to project.version,
            "mcVersion" to mcVersion,
            "neoVersion" to neoForgeVersion,
            "packFormat" to packFormat
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
