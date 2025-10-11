// ─────────────────────────────────────────────
// Root-only tasks: Safe Deep Clean, Assemble All Mods, Fast Rebuild
// ─────────────────────────────────────────────
if (project == rootProject) {

    // ─────────────────────────────────────────────
    // Safe Deep Clean (preserves version folders)
    // ─────────────────────────────────────────────
    tasks.register("deepClean") {
        group = "build"
        description = "Safely removes Gradle caches and build outputs without deleting version folders or configs."

        doLast {
            // Root-level cleanup targets
            val rootTargets = listOf(
                rootProject.file(".gradle"),
                rootProject.file("build"),
                rootProject.file("out"),
                rootProject.file("logs")
            )

            // Internal build outputs inside each version folder
            val versionBuilds = rootProject.file("versions")
                .listFiles()
                ?.filter { it.isDirectory }
                ?.flatMap { versionDir ->
                    listOf(
                        File(versionDir, "build"),
                        File(versionDir, ".gradle"),
                        File(versionDir, "out")
                    )
                } ?: emptyList()

            // Combine and delete
            val allTargets = rootTargets + versionBuilds

            println("🧹 Starting safe deep clean...")
            allTargets.forEach { dir ->
                if (dir.exists()) {
                    println("   - Deleting ${dir.absolutePath}")
                    dir.deleteRecursively()
                }
            }

            println("✅ Safe deep clean complete. Version source folders preserved.")
        }
    }

    // ─────────────────────────────────────────────
    // Assemble All Mods (full clean + multi-version build)
    // ─────────────────────────────────────────────
    tasks.register("assembleAllMods") {
        group = "build"
        description = "Performs a safe deep clean and then builds all Stonecutter version variants of The Expanse."

        // Optional CLI override: -PskipClean=true
        val skipClean = project.findProperty("skipClean")?.toString()?.equals("true", true) == true
        if (!skipClean) dependsOn(rootProject.tasks.named("deepClean"))

        doLast {
            val subprojectsToBuild = rootProject.subprojects.filter {
                it.name.matches(Regex("""\d+\.\d+(\.\d+)?"""))
            }

            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val gradlewCmd = if (isWindows) "gradlew.bat" else "./gradlew"
            val finalDir = rootProject.layout.buildDirectory.dir("libs/final").get().asFile

            println("🚀 Starting builds for ${subprojectsToBuild.size} versions...")
            subprojectsToBuild.parallelStream().forEach { sub ->
                val result = project.providers.exec {
                    if (isWindows)
                        commandLine("cmd", "/c", gradlewCmd, ":${sub.name}:assembleMod", "--no-daemon")
                    else
                        commandLine("bash", "-c", "$gradlewCmd :${sub.name}:assembleMod --no-daemon")

                    workingDir(rootProject.projectDir)
                }.result.get()

                if (result.exitValue != 0) {
                    println("❌ Failed to build ${sub.name}, skipping...")
                } else {
                    val buildDir = File(sub.projectDir, "build/libs/final")
                    if (buildDir.exists()) {
                        buildDir.listFiles { f -> f.extension == "jar" }?.forEach { jar ->
                            val dest = File(finalDir, jar.name)
                            jar.copyTo(dest, overwrite = true)
                            println("📦 Copied ${jar.name} → ${dest.path}")
                        }
                    }
                }
            }

            println("✅ All possible builds complete! Check build/libs/final/")
        }
    }

    // ─────────────────────────────────────────────
    // Fast Rebuild (no clean, just rebuild)
    // ─────────────────────────────────────────────
    tasks.register("fastRebuild") {
        group = "build"
        description = "Rebuilds all Stonecutter subprojects without cleaning or removing caches."

        doLast {
            val subprojectsToBuild = rootProject.subprojects.filter {
                it.name.matches(Regex("""\d+\.\d+(\.\d+)?"""))
            }

            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val gradlewCmd = if (isWindows) "gradlew.bat" else "./gradlew"
            val finalDir = rootProject.layout.buildDirectory.dir("libs/final").get().asFile

            println("⚡ Fast rebuild: skipping clean. Building modified versions...")
            subprojectsToBuild.parallelStream().forEach { sub ->
                val result = project.providers.exec {
                    if (isWindows)
                        commandLine("cmd", "/c", gradlewCmd, ":${sub.name}:build", "--no-daemon", "--parallel")
                    else
                        commandLine("bash", "-c", "$gradlewCmd :${sub.name}:build --no-daemon --parallel")

                    workingDir(rootProject.projectDir)
                }.result.get()

                if (result.exitValue != 0) {
                    println("❌ Failed to rebuild ${sub.name}, skipping...")
                } else {
                    val buildDir = File(sub.projectDir, "build/libs/final")
                    if (buildDir.exists()) {
                        buildDir.listFiles { f -> f.extension == "jar" }?.forEach { jar ->
                            val dest = File(finalDir, jar.name)
                            jar.copyTo(dest, overwrite = true)
                            println("📦 Updated ${jar.name} → ${dest.path}")
                        }
                    }
                }
            }

            println("✅ Fast rebuild complete! JARs available in build/libs/final/")
        }
    }
}
