import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.Properties

plugins {
    id("net.neoforged.moddev")
    id("maven-publish")
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

dependencyLocking { lockAllConfigurations() }

fun deps(key: String) = providers.gradleProperty("deps.$key").orElse(
    providers.provider {
        project.findProperty("deps.$key")?.toString()
            ?: error("Missing dependency coordinate for 'deps.$key'")
    }
)

val neoForgeVersionProvider = deps("neoforge")
val configuredVersionsProvider = providers.gradleProperty("versionsList")
    .map { raw ->
        raw.split(';')
            .map(String::trim)
            .filter(String::isNotBlank)
    }
val configuredVersions = configuredVersionsProvider.orElse(emptyList()).get()
check(configuredVersions.isNotEmpty()) {
    "No Stonecutter versions were provided via the 'versionsList' property."
}
val primaryVersion = configuredVersions.first()
val datapackRuntimeRunDir = layout.buildDirectory.dir("datapackRuntime/server")

neoForge {
    version = neoForgeVersionProvider
    runs {
        create("datapackRuntime") {
            server()
            programArgument("--nogui")
            disableIdeRun()
            gameDirectory.set(datapackRuntimeRunDir)
        }
    }
}

allprojects {
    tasks.configureEach {
        if (name.equals("deepclean", ignoreCase = true)) {
            enabled = false
        }
    }
}

tasks.register("verifyDependencyLocks") {
    group = "verification"
    description = "Ensures dependency lockfiles are up-to-date and honored."
    doLast { configurations.filter { it.isCanBeResolved }.forEach { it.resolve() } }
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

// ─── Configurable properties ─────────────────────────────────────
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
val useMixins = project.resolveToggle("useMixins", false)
extensions.extraProperties["enableDatagen"] = enableDatagen
extensions.extraProperties["useMixins"] = useMixins

// ─── Metadata ─────────────────────────────────────────────────────
val mcVersion = project.findProperty("MC_VERSION")?.toString() ?: "unspecified"
val mcVersionNext = project.findProperty("MC_VERSION_NEXT")?.toString() ?: "unspecified"
val neoForgeVersion = project.findProperty("NEOFORGE_VERSION")?.toString()
    ?: neoForgeVersionProvider.get()
val packFormat = project.findProperty("PACK_FORMAT")?.toString() ?: "0"
val modVersion = project.findProperty("MOD_VERSION")?.toString() ?: "0.0.0"
val loaderTag = "neoforge"

extensions.extraProperties["NEOFORGE_VERSION"] = neoForgeVersion

group = "com.theexpanse"
version = modVersion

// ─── Dependencies ──────────────────────────────────────────────────
repositories {
    maven("https://maven.neoforged.net/releases")
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation("org.spongepowered:mixin:0.15.2") {
        isTransitive = false
    }
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

// ─── Code Quality ──────────────────────────────────────────────────
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

// ─── Resource Token Expansion (safe fallback) ─────────────────────
tasks.processResources {
    val modVersion = project.findProperty("MOD_VERSION")?.toString() ?: "0.0.0"
    val mcVersion = project.findProperty("MC_VERSION")?.toString() ?: "unknown"
    val neoForgeVersion = project.findProperty("NEOFORGE_VERSION")?.toString() ?: "?"
    val packFormat = project.findProperty("PACK_FORMAT")?.toString() ?: "0"
    val mcVersionNext = project.findProperty("MC_VERSION_NEXT")?.toString() ?: "unspecified"

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

// ─── Jar Naming ────────────────────────────────────────────────────
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

tasks.register("buildApp") {
    group = "build"
    description = "Builds the primary Stonecutter variant ($primaryVersion)."
    dependsOn(":$primaryVersion:build")
}

tasks.register("buildAllApp") {
    group = "build"
    description = "Builds every configured Stonecutter variant."
    dependsOn(configuredVersions.map { ":$it:build" })
}

tasks.register("allClean") {
    group = "build"
    description = "Cleans all configured Stonecutter variants."
    dependsOn(configuredVersions.map { ":$it:clean" })
}

// ───────────────────────────────────────────────
// Deep clean task (safe version)
// ───────────────────────────────────────────────
tasks.register("deepClean") {
    group = "maintenance"
    description = "Removes Gradle caches, build outputs, and temporary files safely."

    doLast {
        val targets = listOf(
            ".gradle",
            "build",
            "stonecutter-cache"
        ) + file("versions").listFiles()?.map { "${it.path}/build" }.orEmpty()

        targets.forEach { path ->
            val f = file(path)
            if (f.exists()) {
                println("🧹 Deleting ${f.absolutePath}")
                f.deleteRecursively()
            }
        }
        println("✅ Deep clean complete.")
    }
}

// ───────────────────────────────────────────────
// Global assembleAllMods task (root-safe)
// ───────────────────────────────────────────────
tasks.register("assembleAllMods") {
    group = "build"
    description = "Runs deep clean, then builds all Stonecutter NeoForge mod versions."

    dependsOn("deepClean")

    doLast {
        println("🧱 Performing deep clean + build...")

        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val gradlewCmd = if (isWindows) "gradlew.bat" else "./gradlew"
        val variants = file("versions").listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()

        if (variants.isEmpty()) {
            println("⚠️ No version folders found under /versions — skipping multi-build.")
            return@doLast
        }

        variants.forEach { ver ->
            println("🚀 Building $ver...")
            project.providers.exec {
                workingDir(rootProject.projectDir)
                if (isWindows) {
                    commandLine("cmd", "/c", gradlewCmd, ":$ver:assembleMod", "--no-daemon")
                } else {
                    commandLine("bash", "-c", "$gradlewCmd :$ver:assembleMod --no-daemon")
                }
            }.result.get()
        }

        println("✅ All versions built successfully! Check build/libs/final/")
    }
}

// ─── Publishing ───────────────────────────────────────────────────
publishing {
    publications {
        create<MavenPublication>("mavenJava") { artifact(tasks["jar"]) }
    }
}
