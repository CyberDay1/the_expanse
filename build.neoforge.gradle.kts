import org.gradle.api.Project
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import io.gitlab.arturbosch.detekt.Detekt
import net.neoforged.gradle.dsl.common.runs.run.Run
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.Properties

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

// ─────────────────────────────────────────────
// Dependency Locking
// ─────────────────────────────────────────────
dependencyLocking { lockAllConfigurations() }

tasks.register("verifyDependencyLocks") {
    group = "verification"
    description = "Ensures dependency lockfiles are up-to-date and honored."
    doLast { configurations.filter { it.isCanBeResolved }.forEach { it.resolve() } }
}

// ─────────────────────────────────────────────
// Java Toolchain
// ─────────────────────────────────────────────
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

// ─────────────────────────────────────────────
// Configurable properties
// ─────────────────────────────────────────────
val configurableProperties = Properties().apply {
    val configFile = rootProject.file("configurable.properties")
    if (configFile.exists()) configFile.inputStream().use { load(it) }
}

fun Project.resolveToggle(key: String, default: Boolean): Boolean {
    val cliOverride = findProperty(key)?.toString()?.lowercase()
    val fileValue = configurableProperties.getProperty(key)?.lowercase()
    val resolved = cliOverride ?: fileValue
    return resolved?.let { it == "true" } ?: default
}

val enableDatagen = project.resolveToggle("enableDatagen", true)
val useMixins = project.resolveToggle("useMixins", true) // Force mixins ON
extensions.extraProperties["enableDatagen"] = enableDatagen
extensions.extraProperties["useMixins"] = useMixins

// ─────────────────────────────────────────────
// Minecraft + Mod Metadata
// ─────────────────────────────────────────────
val mcVersion = project.property("MC_VERSION").toString()
val mcVersionNext = project.findProperty("MC_VERSION_NEXT")?.toString() ?: "unspecified"
val neoForgeVersion = project.property("NEOFORGE_VERSION").toString()
val packFormat = project.property("PACK_FORMAT").toString()
val modVersion = project.property("MOD_VERSION").toString()
val loaderTag = "neoforge"

group = "com.theexpanse"
version = modVersion

@Suppress("UNCHECKED_CAST")
val runs = extensions.getByName("runs") as NamedDomainObjectContainer<Run>
val datapackRuntimeRunDir = layout.buildDirectory.dir("datapackRuntime/server")

runs.register("datapackRuntime") {
    run("server")
    arguments.add("--nogui")
    shouldExportToIDE(false)
    workingDirectory(datapackRuntimeRunDir.get().asFile)
}

// ─────────────────────────────────────────────
// Repositories and Dependencies
// ─────────────────────────────────────────────
repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://libraries.minecraft.net/")
    mavenCentral()
    mavenLocal()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty") useVersion("4.1.118.Final")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
}

// ─────────────────────────────────────────────
// Code Quality
// ─────────────────────────────────────────────
spotless {
    java {
        target("template/src/**/*.java")
        googleJavaFormat("1.17.0")
        removeUnusedImports()
        importOrder("", "java", "javax", "org", "com")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts", "../*.gradle.kts", "template/**/*.gradle.kts", "versions/**/*.gradle.kts")
        ktlint()
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
    reports { html.required.set(true); xml.required.set(true) }
}

// ─────────────────────────────────────────────
// Resource Token Expansion (Fixed)
// ─────────────────────────────────────────────
tasks.processResources {
    val modVersion = project.findProperty("MOD_VERSION")?.toString() ?: "1.0.0"
    val mcVersion = project.findProperty("MC_VERSION")?.toString() ?: "unknown"
    val mcVersionNext = project.findProperty("MC_VERSION_NEXT")?.toString() ?: "unspecified"
    val neoForgeVersion = project.findProperty("NEOFORGE_VERSION")?.toString() ?: "0.0.0"
    val packFormat = project.findProperty("PACK_FORMAT")?.toString() ?: "0"

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "MOD_VERSION" to modVersion,
                "MC_VERSION" to mcVersion,
                "MC_VERSION_NEXT" to mcVersionNext,
                "NEOFORGE_VERSION" to neoForgeVersion
            )
        )
    }

    filesMatching("pack.mcmeta") {
        expand(
            mapOf(
                "MOD_VERSION" to modVersion,
                "MC_VERSION" to mcVersion,
                "MC_VERSION_NEXT" to mcVersionNext,
                "NEOFORGE_VERSION" to neoForgeVersion,
                "PACK_FORMAT" to packFormat
            )
        )
    }
}

// ─────────────────────────────────────────────
// Jar Naming
// ─────────────────────────────────────────────
tasks.withType<Jar>().configureEach {
    val modName = "the_expanse"
    val mcVersionTag = project.findProperty("MC_VERSION")?.toString()
        ?: project.name.substringBefore("-")

    archiveBaseName.set("$modName-$loaderTag")
    archiveVersion.set(mcVersionTag)
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/final"))

    doLast {
        val outFile = archiveFile.get().asFile
        val renamed = File(destinationDirectory.get().asFile, "$modName-$loaderTag-$mcVersionTag.jar")
        if (outFile.exists() && outFile.name != renamed.name) {
            outFile.copyTo(renamed, overwrite = true)
            outFile.delete()
            println("✅ Renamed ${outFile.name} → ${renamed.name}")
        }
    }
}

tasks.register("assembleMod") {
    group = "build"
    description = "Assembles the The Expanse mod JAR with clean naming"
    dependsOn("jar")
}

// ─────────────────────────────────────────────
// Deep Clean (Clears Caches + Version Repos)
// ─────────────────────────────────────────────
tasks.register("deepClean") {
    group = "build"
    description = "Removes Gradle caches, version repos, and build outputs for a truly clean rebuild."

    doLast {
        val dirsToDelete = listOf(
            rootProject.file(".gradle"),
            rootProject.file("build")
        ) + rootProject.file("versions").listFiles()?.mapNotNull {
            it.takeIf { sub -> sub.isDirectory && File(sub, ".gradle").exists() }
        }.orEmpty()

        dirsToDelete.forEach {
            if (it.exists()) {
                println("🧹 Deleting ${it.absolutePath}")
                it.deleteRecursively()
            }
        }
        println("✅ Deep clean complete.")
    }
}

// ─────────────────────────────────────────────
// Build All Versions (with optional skip-clean)
// ─────────────────────────────────────────────
tasks.register("assembleAllMods") {
    group = "build"
    description = "Builds all Stonecutter version variants of The Expanse. Use -PskipClean=true to skip deep cleaning."

    val skipClean = project.findProperty("skipClean")?.toString()?.equals("true", ignoreCase = true) == true
    if (!skipClean) dependsOn("deepClean")

    doLast {
        println(if (skipClean) "⚙️ Skipping deep clean (per -PskipClean flag)" else "🧱 Performing deep clean + build...")

        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val gradlewCmd = if (isWindows) "gradlew.bat" else "./gradlew"

        rootProject.subprojects
            .filter { it.name.matches(Regex("""\d+\.\d+(\.\d+)?""")) }
            .forEach { sub ->
                println("🚀 Building ${sub.name}...")
                project.providers.exec {
                    if (isWindows) {
                        commandLine("cmd", "/c", gradlewCmd, ":${sub.name}:assembleMod", "--no-daemon")
                    } else {
                        commandLine("bash", "-c", "$gradlewCmd :${sub.name}:assembleMod --no-daemon")
                    }
                    workingDir(rootProject.projectDir)
                }.result.get()
            }

        println("✅ All versions built successfully! Check build/libs/final/")
    }
}

// ─────────────────────────────────────────────
// Publishing
// ─────────────────────────────────────────────
publishing {
    publications {
        create<MavenPublication>("mavenJava") { artifact(tasks["jar"]) }
    }
}
