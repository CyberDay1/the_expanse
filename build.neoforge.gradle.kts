import org.gradle.api.Project
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.GradleException
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import io.gitlab.arturbosch.detekt.Detekt
import net.neoforged.gradle.dsl.common.runs.run.Run
import org.gradle.api.tasks.JavaExec
import java.io.File
import java.util.Properties
import org.gradle.jvm.tasks.Jar

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

tasks.register("verifyDependencyLocks") {
    group = "verification"
    description = "Ensures dependency lockfiles are up-to-date and honored."
    doLast { configurations.filter { it.isCanBeResolved }.forEach { it.resolve() } }
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

// ───────────────────────────────────────────────
// Configurable toggles
// ───────────────────────────────────────────────
val configurableProperties = Properties().apply {
    val configFile = rootProject.file("configurable.properties")
    if (configFile.exists()) configFile.inputStream().use { load(it) }
}
fun Project.resolveToggle(key: String, default: Boolean): Boolean {
    val cliOverride = findProperty(key)?.toString()?.lowercase()
    val fileValue = configurableProperties.getProperty(key)?.lowercase()
    val resolved = cliOverride ?: fileValue
    return resolved?.let { it == "true" } ?: default
}
val enableDatagen = project.resolveToggle("enableDatagen", true)
val useMixins = project.resolveToggle("useMixins", false)
extensions.extraProperties["enableDatagen"] = enableDatagen
extensions.extraProperties["useMixins"] = useMixins

// ───────────────────────────────────────────────
// Core versioning
// ───────────────────────────────────────────────
val mcVersion = project.property("MC_VERSION").toString()
val mcVersionNext = project.property("MC_VERSION_NEXT").toString()
val neoForgeVersion = project.property("NEOFORGE_VERSION").toString()
val packFormat = project.property("PACK_FORMAT").toString()
val modVersion = project.property("MOD_VERSION").toString()

group = "com.theexpanse"
version = modVersion

@Suppress("UNCHECKED_CAST")
val runs = extensions.getByName("runs") as NamedDomainObjectContainer<Run>
val datapackRuntimeRunDir = layout.buildDirectory.dir("datapackRuntime/server")

runs.register("datapackRuntime") {
    run("server")
    arguments.add("--nogui")
    shouldExportToIDE(false)
    workingDirectory(datapackRuntimeRunDir.get().asFile)
}

// ───────────────────────────────────────────────
// Dependency setup
// ───────────────────────────────────────────────
repositories {
    mavenLocal()
    mavenCentral()

    // NeoForge official repos
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.neoforged.net/snapshots")

    // Mojang (Minecraft) libraries
    maven("https://libraries.minecraft.net")

    // Forge mirror for Mojang artifacts and legacy mods
    maven("https://maven.minecraftforge.net")

    // Fallbacks for common missing artifacts
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://jitpack.io")
}


configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty") useVersion("4.1.118.Final")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
}

// ───────────────────────────────────────────────
// Formatting / Lint / Checks
// ───────────────────────────────────────────────
spotless {
    java {
        target("template/src/**/*.java", "${rootProject.projectDir}/template/src/**/*.java")
        licenseHeaderFile(rootProject.file("config/spotless/license-header.java"), "(package|import)")
        googleJavaFormat("1.17.0")
        removeUnusedImports()
        importOrder("", "java", "javax", "org", "com")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("template/src/**/*.kt", "${rootProject.projectDir}/template/src/**/*.kt")
        licenseHeaderFile(rootProject.file("config/spotless/license-header.kt"), "(package|import)")
        ktlint().editorConfigOverride(mapOf("indent_size" to "4", "continuation_indent_size" to "4"))
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts", "../*.gradle.kts", "buildSrc/**/*.gradle.kts", "template/**/*.gradle.kts", "versions/**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint().editorConfigOverride(mapOf("indent_size" to "4", "continuation_indent_size" to "4"))
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "10.17.0"
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    isIgnoreFailures = false
}

val detektConfig = rootProject.files("config/detekt/detekt.yml")

detekt {
    buildUponDefaultConfig = true
    config.setFrom(detektConfig)
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.register<Detekt>("detektMain") {
    description = "Runs Detekt analysis on the main Kotlin sources."
    group = "verification"
    buildUponDefaultConfig = true
    config.setFrom(detektConfig)
    setSource(files("template/src/main/kotlin", rootProject.layout.projectDirectory.dir("template/src/main/kotlin")))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/.gradle/**")
}

tasks.named("check") {
    dependsOn("spotlessCheck", "checkstyleMain", "detektMain")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

// ───────────────────────────────────────────────
// Resource expansion (mods.toml / mcmeta)
// ───────────────────────────────────────────────
tasks.processResources {
    inputs.property("modVersion", modVersion)
    inputs.property("mcVersion", mcVersion)
    inputs.property("mcVersionNext", mcVersionNext)
    inputs.property("neoVersion", neoForgeVersion)
    inputs.property("packFormat", packFormat)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "MOD_VERSION" to modVersion,
                "MC_VERSION" to mcVersion,
                "MC_VERSION_NEXT" to mcVersionNext,
                "NEOFORGE_VERSION" to neoForgeVersion
            )
        )
    }

    filesMatching("pack.mcmeta") {
        expand(
            mapOf(
                "MOD_VERSION" to modVersion,
                "MC_VERSION" to mcVersion,
                "NEOFORGE_VERSION" to neoForgeVersion,
                "PACK_FORMAT" to packFormat
            )
        )
    }
}

// ───────────────────────────────────────────────
// Publishing setup
// ───────────────────────────────────────────────
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
        }
    }
}

// ───────────────────────────────────────────────
// Stonecutter integration
// ───────────────────────────────────────────────
val configuredVersions = project.rootProject.file("versions")
    .listFiles()
    ?.filter { it.isDirectory }
    ?.map { it.name }
    ?.sorted()
    ?: emptyList()

if (configuredVersions.isEmpty()) {
    throw GradleException("No Stonecutter version directories found under /versions.")
}

val primaryVersion = configuredVersions.first()

tasks.register("buildApp") {
    group = "build"
    description = "Builds the primary Stonecutter variant ($primaryVersion)."
    dependsOn(":$primaryVersion:build")
}

tasks.register("buildAllApp") {
    group = "build"
    description = "Builds every configured Stonecutter variant."
    dependsOn(configuredVersions.map { ":$it:build" })
}

tasks.register("allClean") {
    group = "build"
    description = "Cleans all configured Stonecutter variants and removes old jars from build/libs."
    doFirst {
        val libsDir = rootProject.layout.buildDirectory.dir("libs").get().asFile
        if (libsDir.exists()) {
            println("🧹 Removing existing jars from ${libsDir.absolutePath}")
            libsDir.listFiles()?.forEach { it.delete() }
        }
    }
    dependsOn(configuredVersions.map { ":$it:clean" })
}

tasks.register("deepClean") {
    group = "maintenance"
    description = "Safely removes Gradle caches and build outputs without deleting version folders."

    doLast {
        val rootTargets = listOf(
            rootProject.file(".gradle"),
            rootProject.file("build"),
            rootProject.file("out"),
            rootProject.file("logs")
        )

        val versionBuilds = rootProject.file("versions").listFiles()
            ?.filter { it.isDirectory }
            ?.flatMap { listOf(File(it, "build"), File(it, ".gradle")) }
            ?: emptyList()

        println("🧹 Starting safe deep clean...")
        (rootTargets + versionBuilds).forEach { dir ->
            if (dir.exists()) {
                println("   - Deleting ${dir.absolutePath}")
                dir.deleteRecursively()
            }
        }
        println("✅ Safe deep clean complete. Version source folders preserved.")
    }
}

// ───────────────────────────────────────────────
// Jar relocation and rename
// ───────────────────────────────────────────────
gradle.projectsEvaluated {
    allprojects.forEach { sub ->
        sub.tasks.matching { it.name.equals("jar", true) || it.name.equals("jarJar", true) }
            .configureEach {
                this as Jar
                destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))

                doLast {
                    val mcVersion = sub.findProperty("MC_VERSION")?.toString() ?: "unknown"
                    val modVersion = sub.findProperty("MOD_VERSION")?.toString() ?: "dev"
                    val destDir = rootProject.layout.buildDirectory.dir("libs").get().asFile
                    destDir.mkdirs()

                    val sourceFile = archiveFile.get().asFile
                    if (sourceFile.exists()) {
                        val sanitizedName = "TheExpanse-NeoForge+$mcVersion-$modVersion.jar"
                        val targetFile = File(destDir, sanitizedName)

                        destDir.listFiles()
                            ?.filter { it.name.contains("TheExpanse-NeoForge+$mcVersion") }
                            ?.forEach { it.delete() }

                        if (sourceFile.renameTo(targetFile)) {
                            println("✅ Moved ${sub.name} jar → ${targetFile.name}")
                        } else {
                            println("⚠️ Failed to move ${sourceFile.name} for ${sub.name}")
                        }
                    } else println("⚠️ No jar found for ${sub.name}")
                }
            }
    }
}

// ───────────────────────────────────────────────
// Fix: prevent dev runs from loading build/resources/main as a mod
// ───────────────────────────────────────────────
afterEvaluate {
    allprojects.forEach { sub ->
        sub.extensions.findByName("runs")?.let { runsExt ->
            @Suppress("UNCHECKED_CAST")
            val runs = runsExt as NamedDomainObjectContainer<Run>

            runs.configureEach {
                modSources {
                    // remove the main sourceset to avoid the “directory mod” error
                    sourceSets.removeIf { it.name == "main" }
                }
            }
        }
    }
}
