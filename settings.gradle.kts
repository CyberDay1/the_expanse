pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
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
        set("JAVA_VERSION", settings.extra["JAVA_VERSION"].toString())
    }
    kotlin.set(false)

    // Define all 1.21.x NeoForge variants with correct pack_format mapping
    (1..9).forEach { patch ->
        create("1.21.${patch}-neoforge") {
            set("MC_VERSION", settings.extra["MC_1_21_${patch}"].toString())
            set("NEOFORGE_VERSION", settings.extra["NEOFORGE_1_21_${patch}"].toString())

            // Map correct pack_format
            val pf = when (patch) {
                1 -> 48
                2, 3 -> 57
                4 -> 61
                5 -> 71
                6 -> 80
                7, 8 -> 81
                9 -> 88
                else -> 48
            }
            set("PACK_FORMAT", pf.toString())
        }
    }
}

// Include all variant projects
(1..9).forEach { patch ->
    include(":1.21.${patch}-neoforge")
}
