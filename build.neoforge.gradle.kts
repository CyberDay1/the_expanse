import org.gradle.api.tasks.bundling.Jar
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("net.neoforged.gradle") version "6.0.18" apply false
    id("dev.kikugie.stonecutter") version "0.7.10"
}

val modName = "the_expanse"
val modVersion = "1.0.0"

gradle.taskGraph.whenReady {
    allTasks.forEach { t ->
        if (t.name.contains("neoForm", ignoreCase = true)) {
            println("⚙️  Disabling incremental/state tracking for ${t.path}")
            t.outputs.upToDateWhen { false }
            t.doNotTrackState("NeoForm instability workaround")
        }
    }
}

tasks.register("safeDeepClean") {
    group = "build"
    description = "Safely remove build caches without touching sources."
    doLast {
        println("🧹 Safe deep clean …")
        delete(rootProject.file(".gradle"), rootProject.file("build"))
        rootProject.file("versions").listFiles()?.forEach { ver ->
            val buildDir = ver.resolve("build")
            if (buildDir.exists()) {
                println("🗑️  Removing $buildDir")
                buildDir.deleteRecursively()
            }
        }
        println("✅ Safe deep clean complete.")
    }
}

tasks.register("assembleAllMods") {
    group = "build"
    description = "Build all Stonecutter variants sequentially with logs."

    doLast {
        val logDir = file("build-logs").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
        val logFile = logDir.resolve("build-$timestamp.log")

        println("🚀 Building all versions … (logging to ${logFile.name})")
        val versionsDir = file("versions")
        versionsDir.listFiles()?.sorted()?.forEach { verDir ->
            if (verDir.isDirectory) {
                val msg = "🔧 Building ${verDir.name}"
                println(msg)
                logFile.appendText("$msg\n")
                exec {
                    commandLine("gradlew", ":${verDir.name}:build")
                }
            }
        }
        println("✅ All builds complete — log saved to ${logFile.absolutePath}")
    }
}
