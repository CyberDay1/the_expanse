plugins {
    id("java")
    id("net.neoforged.gradle") version "6.0.18"
}

// ───────────────────────────────
//  Java toolchain
// ───────────────────────────────
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

// ───────────────────────────────
//  Base mod metadata
// ───────────────────────────────
group = "com.cyberday"
version = "1.0.0" // overridden later

// ───────────────────────────────
//  NeoForge setup
// ───────────────────────────────
minecraft {
    mappings("official", stonecutter["MC_VERSION"])
}

dependencies {
    implementation("net.neoforged:neoforge:${stonecutter["NEOFORGE_VERSION"]}")
}

// ───────────────────────────────
//  Custom JAR task (per-version)
// ───────────────────────────────
tasks.register<Jar>("assembleMod") {
    val mcVersion = stonecutter["MC_VERSION"]
    val modName = "the_expanse"

    group = "build"
    description = "Assembles the $modName mod JAR for Minecraft $mcVersion."

    from(sourceSets.main.get().output)

    // Set clean naming
    archiveBaseName.set(modName)
    archiveVersion.set(mcVersion)
    destinationDirectory.set(layout.buildDirectory.dir("libs/final"))

    manifest {
        attributes(
            "Implementation-Title" to modName,
            "Implementation-Version" to mcVersion,
            "Specification-Title" to "Minecraft Mod",
            "Specification-Version" to "NeoForge"
        )
    }
}

// Run our custom JAR after standard build
tasks.named("build").configure {
    finalizedBy("assembleMod")
}
