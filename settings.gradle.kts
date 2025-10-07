pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        mavenCentral()
    }
}

plugins {
    // Must be dev.kikugie, not dev.kayla
    id("dev.kikugie.stonecutter") version "0.7.10"
}

rootProject.name = "the_expanse"

stonecutter {
    active("1.21.1-neoforge")

    versions {
        register("1.21.1-neoforge") {
            vcsVersion("1.21.1")
            data["NEOFORGE_VERSION"] = "21.1.209"
        }
        register("1.21.2-neoforge") {
            vcsVersion("1.21.2")
            data["NEOFORGE_VERSION"] = "21.2.0-beta"
        }
    }
}
