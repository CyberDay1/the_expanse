import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import io.gitlab.arturbosch.detekt.Detekt
import java.util.Properties

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.register("verifyDependencyLocks") {
    group = "verification"
    description = "Ensures dependency lockfiles are up-to-date and honored."
    doLast {
        configurations
            .filter { it.isCanBeResolved }
            .forEach { it.resolve() }
    }
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
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("config/spotless/license-header.java"), "(package|import)")
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlin {
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.file("config/spotless/license-header.kt"), "(package|import)")
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target(
            "*.gradle.kts",
            "buildSrc/**/*.gradle.kts",
            "template/**/*.gradle.kts",
            "versions/**/*.gradle.kts"
        )
        targetExclude("**/build/**", "**/.gradle/**")
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "10.17.0"
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    isIgnoreFailures = false
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        txt.required.set(false)
        xml.required.set(true)
        sarif.required.set(false)
    }
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck", "detekt")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
