plugins {
    id("java")
    id("net.neoforged.gradle") version "6.0.18"
}

// --- Apply Java 21 toolchain to mod template ---
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

// --- Base mod metadata ---
group = "com.cyberday"
version = "1.0.0"

// --- NeoForge configuration ---
minecraft {
    mappings("official", stonecutter["MC_VERSION"])
}

dependencies {
    implementation("net.neoforged:neoforge:${stonecutter["NEOFORGE_VERSION"]}")
}

// --- Enforce proper jar naming after Stonecutter version injection ---
afterEvaluate {
    tasks.withType<Jar>().configureEach {
        val mcVersion = project.name.substringBefore("-")
        archiveBaseName.set("the_expanse")
        archiveVersion.set(mcVersion)
        destinationDirectory.set(layout.buildDirectory.dir("libs/${mcVersion}"))
    }

    // Also override the project version to keep it consistent
    version = project.name.substringBefore("-")
}
