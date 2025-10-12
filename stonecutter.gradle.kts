plugins {
    id("net.neoforged.gradle") version "6.0.18" apply false
}

stonecutter {
    project(":") {
        // Each version inherits its configuration dynamically
        versioned("build.neoforge.gradle.kts")
    }
}
