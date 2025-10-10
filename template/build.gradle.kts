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

// Dynamically infer the version from the Stonecutter project name BEFORE NeoForge sets up
val mcVersion = project.name.substringBefore("-")
version = mcVersion

// --- NeoForge configuration ---
minecraft {
    mappings("official", stonecutter["MC_VERSION"])
}

dependencies {
    implementation("net.neoforged:neoforge:${stonecutter["NEOFORGE_VERSION"]}")
}

// --- Enforce consistent JAR naming ---
tasks.withType<Jar>().configureEach {
    val modName = "the_expanse"
    val mcVersionLocal = project.name.substringBefore("-")
    archiveBaseName.set(modName)
    archiveVersion.set(mcVersionLocal)
    destinationDirectory.set(layout.buildDirectory.dir("libs/$mcVersionLocal"))
}
