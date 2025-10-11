import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.register
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("net.neoforged.gradle") version "6.0.18" apply false
    id("dev.kikugie.stonecutter") version "0.7.10"
    id("idea")
}

// ───────────────────────────────
// Version + Mod Info
// ───────────────────────────────
val allVersions: List<String> = stonecutter.versions.map { it.toString() }

val modName = "the_expanse"
val modVersion = "1.0.0"

val neoForgeVersions = mapOf(
    "1.21.1" to "21.1.209",
    "1.21.2" to "21.2.1-beta",
    "1.21.3" to "21.3.93",
    "1.21.4" to "21.4.154",
    "1.21.5" to "21.5.95",
    "1.21.6" to "21.6.20-beta",
    "1.21.7" to "21.7.25-beta",
    "1.21.8" to "21.8.47",
    "1.21.9" to "21.9.16-beta",
    "1.21.10" to "21.10.5-beta"
)

// ───────────────────────────────
// Disable NeoForm incremental caching
// ───────────────────────────────
gradle.taskGraph.whenReady {
    allTasks.forEach { t ->
        if (t.name.contains("neoForm", ignoreCase = true)) {
            println("⚙️  Disabling incremental/state tracking for ${t.path}")
            t.outputs.upToDateWhen { false }
            t.doNotTrackState("NeoForm instability workaround")
        }
    }
}

// ───────────────────────────────
// Safe Deep Clean
// ───────────────────────────────
tasks.register("safeDeepClean") {
    group = "build"
    description = "Safely remove build caches without touching source."
    doLast {
        println("🧹 Safe deep clean …")
        delete(rootProject.file(".gradle"), rootProject.file("build"))
        allVersions.forEach { ver ->
            val buildDir = rootProject.file("versions/$ver/build")
            if (buildDir.exists()) {
                println("🗑️  Removing $buildDir")
                buildDir.deleteRecursively()
            }
        }
        println("✅ Safe deep clean complete.")
    }
}

// ───────────────────────────────
// Per-Version Setup
// ───────────────────────────────
allVersions.forEach { ver ->
    val verDir = file("versions/$ver")
    if (verDir.exists()) {
        project(":$ver") {
            apply(plugin = "net.neoforged.gradle")

            println("🔧 Configuring The Expanse → MC $ver • NeoForge ${neoForgeVersions[ver]}")
            group = "com.theexpanse"
            version = modVersion

            repositories {
                maven("https://maven.neoforged.net/releases")
                mavenCentral()
            }

            dependencies {
                val neoVersion = neoForgeVersions[ver]
                    ?: error("Missing NeoForge version for Minecraft $ver")
                implementation("net.neoforged:neoforge:$neoVersion")
            }

            // Proper jar naming: the_expanse-<mcver>-1.0.0.jar
            tasks.withType<Jar> {
                archiveBaseName.set("$modName-$ver")
                archiveVersion.set(modVersion)
            }

            tasks.register("assembleMod") {
                group = "build"
                description = "Assemble mod for MC $ver"
                dependsOn("build")
            }
        }
    }
}

// ───────────────────────────────
// Build All Versions (with logging)
// ───────────────────────────────
tasks.register("assembleAllMods") {
    group = "build"
    description = "Builds all Stonecutter versions sequentially and logs output."

    dependsOn(
        allVersions.mapNotNull { ver ->
            val verDir = file("versions/$ver")
            if (verDir.exists()) ":$ver:assembleMod" else null
        }
    )

    doFirst {
        val logDir = file("build-logs").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
        val logFile = logDir.resolve("build-$timestamp.log")

        println("🚀 Building all versions … (logging to ${logFile.name})")
        logFile.writeText("=== Build started at $timestamp ===\n")

        allVersions.forEach { ver ->
            val verDir = file("versions/$ver")
            if (!verDir.exists()) {
                val msg = "⚠️  Skipping $ver — no directory found"
                println(msg)
                logFile.appendText("$msg\n")
            } else {
                val msg = "🔧 Scheduled build for $ver"
                println(msg)
                logFile.appendText("$msg\n")
            }
        }
    }

    doLast {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
        val logFile = file("build-logs/build-$timestamp.log")
        logFile.appendText("✅ All builds complete.\n")
        println("✅ All builds complete — log saved to ${logFile.absolutePath}")
    }
}

// ───────────────────────────────
// NeoForm Fallback
// ───────────────────────────────
tasks.configureEach {
    if (name == "neoFormRecompile") {
        println("⚙️  Applying absolute fallback for $path")
        outputs.upToDateWhen { false }
        doNotTrackState("Force clean NeoForm recompile")
        doFirst {
            val cache = file("build/neoForm")
            if (cache.exists()) {
                println("🧹 Removing stale NeoForm cache: ${cache.absolutePath}")
                cache.deleteRecursively()
            }
        }
    }
}
