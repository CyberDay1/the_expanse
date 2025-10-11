// ─────────────────────────────────────────────
// Root-only tasks: Deep Clean, Assemble All Mods, Fast Rebuild
// ─────────────────────────────────────────────
if (project == rootProject) {

    // ─────────────────────────────────────────────
    // Deep Clean
    // ─────────────────────────────────────────────
    tasks.register("deepClean") {
        group = "build"
        description = "Removes Gradle caches, version repos, and build outputs for a clean rebuild."

        doLast {
            val dirsToDelete: List<File> =
                listOf(rootProject.file(".gradle"), rootProject.file("build")) +
                        (rootProject.file("versions").listFiles()?.filter { it.isDirectory }?.toList() ?: emptyList())

            dirsToDelete.forEach { dir ->
                if (dir.exists()) {
                    println("🧹 Deleting ${dir.absolutePath}")
                    dir.deleteRecursively()
                }
            }
            println("✅ Deep clean complete.")
        }
    }

    // ─────────────────────────────────────────────
    // Assemble All Mods (full clean + build)
    // ─────────────────────────────────────────────
    tasks.register("assembleAllMods") {
        group = "build"
        description = "Performs a deep clean and then builds all Stonecutter version variants of The Expanse."

        val skipClean = project.findProperty("skipClean")?.toString()?.equals("true", true) == true
        if (!skipClean) dependsOn(rootProject.tasks.named("deepClean"))

        doLast {
            val subprojectsToBuild = rootProject.subprojects.filter { it.name.matches(Regex("""\d+\.\d+(\.\d+)?""")) }
            val gradlewCmd = if (System.getProperty("os.name").lowercase().contains("win")) "gradlew.bat" else "./gradlew"
            val finalDir = rootProject.layout.buildDirectory.dir("libs/final").get().asFile

            println("🚀 Starting builds for ${subprojectsToBuild.size} versions...")
            subprojectsToBuild.parallelStream().forEach { sub ->
                val result = project.providers.exec {
                    commandLine("cmd", "/c", gradlewCmd, ":${sub.name}:assembleMod", "--no-daemon")
                    workingDir(rootProject.projectDir)
                }.result.get()

                if (result.exitValue != 0) throw GradleException("❌ Failed to build ${sub.name}")

                // Copy jar from subproject to build/libs/final
                val buildDir = File(sub.projectDir, "build/libs/final")
                if (buildDir.exists()) {
                    buildDir.listFiles { f -> f.extension == "jar" }?.forEach { jar ->
                        val dest = File(finalDir, jar.name)
                        jar.copyTo(dest, overwrite = true)
                        println("📦 Copied ${jar.name} → ${dest.path}")
                    }
                }
            }

            println("✅ All builds complete! Check build/libs/final/")
        }
    }

    // ─────────────────────────────────────────────
    // Fast Rebuild (no cleaning, just build changed mods)
    // ─────────────────────────────────────────────
    tasks.register("fastRebuild") {
        group = "build"
        description = "Rebuilds only the changed subprojects without cleaning caches or intermediate files."

        doLast {
            val subprojectsToBuild = rootProject.subprojects.filter { it.name.matches(Regex("""\d+\.\d+(\.\d+)?""")) }
            val gradlewCmd = if (System.getProperty("os.name").lowercase().contains("win")) "gradlew.bat" else "./gradlew"
            val finalDir = rootProject.layout.buildDirectory.dir("libs/final").get().asFile

            println("⚡ Fast rebuild: skipping clean. Building modified versions...")
            subprojectsToBuild.parallelStream().forEach { sub ->
                val result = project.providers.exec {
                    commandLine("cmd", "/c", gradlewCmd, ":${sub.name}:build", "--no-daemon", "--parallel")
                    workingDir(rootProject.projectDir)
                }.result.get()

                if (result.exitValue != 0) throw GradleException("❌ Failed to rebuild ${sub.name}")

                // Copy jars again for convenience
                val buildDir = File(sub.projectDir, "build/libs/final")
                if (buildDir.exists()) {
                    buildDir.listFiles { f -> f.extension == "jar" }?.forEach { jar ->
                        val dest = File(finalDir, jar.name)
                        jar.copyTo(dest, overwrite = true)
                        println("📦 Updated ${jar.name} → ${dest.path}")
                    }
                }
            }

            println("✅ Fast rebuild complete! JARs updated in build/libs/final/")
        }
    }
}
