// settings.gradle.kts — delayed Stonecutter initialization fix
// Works for multi-version NeoForge setups up to Minecraft 1.21.10

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/releases")
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

// ───────────────────────────────
// Register version subprojects
// ───────────────────────────────
val versionsDir = file("versions")
if (!versionsDir.exists()) error("Missing 'versions' directory at ${versionsDir.absolutePath}")

versionsDir.listFiles()
    ?.filter { it.isDirectory }
    ?.sortedBy { it.name }
    ?.forEach { dir ->
        val name = dir.name.trim()
        include(name)
        project(":$name").projectDir = dir
        println("✅ Registered subproject: $name")
    }

// ───────────────────────────────
// Delay Stonecutter until all includes are done
// ───────────────────────────────
gradle.settingsEvaluated {
    println("🔧 Loading Stonecutter configuration from stonecutter.json after project registration")
    stonecutter {
        create(rootProject, file("stonecutter.json"))
    }
}
