// ─────────────────────────────────────────────────────────────
// 🧱 The Expanse: Unified NeoForge Build (Gradle-9-ready)
// Safe deep clean, assembleAllMods (Windows/Linux safe), clean JAR names
// ─────────────────────────────────────────────────────────────

import java.io.File
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.190"
}

// Access to ExecOperations for Gradle 9 compatibility
val execOps = project.serviceOf<org.gradle.process.ExecOperations>()

// ───────────────────────────────
//  Java setup
// ───────────────────────────────
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

// ───────────────────────────────
//  Project metadata
// ───────────────────────────────
group = "com.cyberday"
version = "1.0.0"

// ───────────────────────────────
//  Property helpers
// ───────────────────────────────
fun propOrEnv(name: String, default: String): String =
    (project.findProperty(name) as? String)
        ?: System.getenv(name)
        ?: default

val mcVersion = propOrEnv("MC_VERSION", "1.21.1")
val mcVersionNext = propOrEnv("MC_VERSION_NEXT", "1.21.99")
val neoForgeVersion = propOrEnv("NEOFORGE_VERSION", "21.1.209")
val modVersion = project.version.toString()
val packFormat = 34

println("🔧 Configuring The Expanse → MC $mcVersion • NeoForge $neoForgeVersion")

// ───────────────────────────────
//  Repositories / Dependencies
// ───────────────────────────────
repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://repo.spongepowered.org/maven")
    maven("https://libraries.minecraft.net")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
    implementation("net.fabricmc:sponge-mixin:0.16.4+mixin.0.8.7")
}

// ───────────────────────────────
//  Resource Expansion
// ───────────────────────────────
tasks.processResources {
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(
            "MOD_VERSION" to modVersion,
            "MC_VERSION" to mcVersion,
            "MC_VERSION_NEXT" to mcVersionNext,
            "NEOFORGE_VERSION" to neoForgeVersion,
            "PACK_FORMAT" to packFormat
        )
    }
}

// ───────────────────────────────
//  Assemble Mod JAR
// ───────────────────────────────
tasks.register<Jar>("assembleMod") {
    group = "build"
    description = "Assembles The Expanse mod JAR for MC $mcVersion"
    val modName = "the_expanse"

    archiveBaseName.set(modName)
    archiveVersion.set("$mcVersion-$modVersion")
    destinationDirectory.set(layout.buildDirectory.dir("libs/final"))
    from(sourceSets.main.get().output)

    manifest {
        attributes(
            "Implementation-Title" to modName,
            "Implementation-Version" to modVersion,
            "Specification-Title" to "Minecraft Mod",
            "Specification-Version" to "NeoForge $neoForgeVersion"
        )
    }

    doLast {
        println("📦 Built ${archiveFile.get().asFile.name}")
    }
}

// Run our JAR build automatically after standard build
tasks.named("build").configure {
    finalizedBy("assembleMod")
}

// ───────────────────────────────
//  Safe Deep Clean
// ───────────────────────────────
tasks.register("deepClean") {
    group = "maintenance"
    description = "Safely cleans build outputs without deleting sources."
    doLast {
        println("🧹 Performing safe deep clean …")

        listOf(".gradle", "build").forEach {
            rootProject.file(it).takeIf(File::exists)?.let { f ->
                println("   Deleting ${f}")
                f.deleteRecursively()
            }
        }

        rootProject.file("versions").listFiles()
            ?.filter(File::isDirectory)
            ?.forEach { dir ->
                File(dir, "build").takeIf(File::exists)?.let { b ->
                    println("   Cleaning ${b}")
                    b.deleteRecursively()
                }
            }

        println("✅ Safe deep clean done.")
    }
}

// ───────────────────────────────
//  Build All Versions (Fixed for Windows Path)
// ───────────────────────────────
tasks.register("assembleAllMods") {
    group = "build"
    description = "Builds all Stonecutter subprojects under versions/."

    doLast {
        println("🚀 Building all versions …")
        val isWin = System.getProperty("os.name").lowercase().contains("win")
        val gradlewPath = rootProject.file("gradlew.bat").absolutePath

        rootProject.file("versions").listFiles()
            ?.filter(File::isDirectory)
            ?.forEach { vDir ->
                println("🔧 Building ${vDir.name} …")
                if (isWin) {
                    execOps.exec {
                        workingDir = rootProject.projectDir
                        commandLine("cmd", "/c", gradlewPath, "${vDir.name}:build")
                    }
                } else {
                    execOps.exec {
                        workingDir = rootProject.projectDir
                        commandLine("bash", "-c", "./gradlew ${vDir.name}:build")
                    }
                }
            }

        println("✅ All versions built successfully.")
    }
}

// ───────────────────────────────
//  Reset Dependency Locks
// ───────────────────────────────
tasks.register("resetDependencyLocks") {
    group = "maintenance"
    description = "Removes and regenerates Gradle lock files."

    doLast {
        rootProject.fileTree(".") { include("**/gradle.lockfile") }.forEach {
            println("🗑️ Removing ${it.path}")
            it.delete()
        }

        println("🔄 Regenerating dependency locks …")
        val isWin = System.getProperty("os.name").lowercase().contains("win")

        if (isWin) {
            execOps.exec {
                workingDir = rootProject.projectDir
                commandLine("cmd", "/c", "gradlew.bat", "dependencies", "--write-locks")
            }
        } else {
            execOps.exec {
                workingDir = rootProject.projectDir
                commandLine("bash", "-c", "./gradlew dependencies --write-locks")
            }
        }

        println("✅ Locks reset successfully.")
    }
}
