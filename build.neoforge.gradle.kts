import org.gradle.api.Project
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.GradleException
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import io.gitlab.arturbosch.detekt.Detekt
import net.neoforged.gradle.dsl.common.runs.run.Run
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.Properties

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190" apply false
    id("maven-publish")
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks.register("verifyDependencyLocks") {
    group = "verification"
    description = "Ensures dependency lockfiles are up-to-date and honored."
    doLast { configurations.filter { it.isCanBeResolved }.forEach { it.resolve() } }
}

// ───────────────────────────────────────────────
// Config toggles
// ───────────────────────────────────────────────
val props = Properties().apply {
    val f = rootProject.file("configurable.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun Project.toggle(key: String, def: Boolean): Boolean {
    val cli = findProperty(key)?.toString()?.lowercase()
    val fileVal = props.getProperty(key)?.lowercase()
    val v = cli ?: fileVal
    return v?.let { it == "true" } ?: def
}
val enableDatagen = project.toggle("enableDatagen", true)
val useMixins = project.toggle("useMixins", false)
extensions.extraProperties["enableDatagen"] = enableDatagen
extensions.extraProperties["useMixins"] = useMixins

// ───────────────────────────────────────────────
// Version info
// ───────────────────────────────────────────────
val mcVersion = property("MC_VERSION").toString()
val mcVersionNext = property("MC_VERSION_NEXT").toString()
val neoForgeVersion = property("NEOFORGE_VERSION").toString()
val packFormat = property("PACK_FORMAT").toString()
val modVersion = property("MOD_VERSION").toString()

group = "com.theexpanse"
version = modVersion

// ───────────────────────────────────────────────
// Shared repos
// ───────────────────────────────────────────────
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.neoforged.net/snapshots")
    maven("https://libraries.minecraft.net")
    maven("https://maven.minecraftforge.net")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://jitpack.io")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty") useVersion("4.1.118.Final")
    }
}

// ───────────────────────────────────────────────
// Static analysis
// ───────────────────────────────────────────────
spotless {
    java {
        target("template/src/**/*.java")
        licenseHeaderFile(rootProject.file("config/spotless/license-header.java"), "(package|import)")
        googleJavaFormat("1.17.0")
        removeUnusedImports()
        importOrder("", "java", "javax", "org", "com")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts", "versions/**/*.gradle.kts")
        ktlint().editorConfigOverride(mapOf("indent_size" to "4"))
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "10.17.0"
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
}

val detektCfg = rootProject.files("config/detekt/detekt.yml")
detekt {
    buildUponDefaultConfig = true
    config.setFrom(detektCfg)
}
tasks.withType<Detekt>().configureEach {
    reports.html.required.set(true)
    reports.xml.required.set(true)
}

tasks.named("check") { dependsOn("spotlessCheck", "checkstyleMain", "detektMain") }

tasks.withType<Test>().configureEach { useJUnitPlatform() }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

// ───────────────────────────────────────────────
// Stonecutter + NeoForge per-version integration
// ───────────────────────────────────────────────
val versionDirs = rootProject.file("versions").listFiles()?.filter { it.isDirectory } ?: emptyList()
if (versionDirs.isEmpty()) throw GradleException("No version folders found in /versions")

versionDirs.forEach { dir ->
    val name = dir.name
    project(":$name") {
        apply(plugin = "net.neoforged.gradle.userdev")

        repositories {
            maven("https://maven.neoforged.net/releases")
            maven("https://maven.neoforged.net/snapshots")
            maven("https://libraries.minecraft.net")
        }

        dependencies {
            // This pulls in Minecraft and NeoForge jars correctly.
            "minecraft"("net.neoforged:neoforge:$neoForgeVersion")
            implementation("com.google.code.gson:gson:2.11.0")
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            dependsOn(":extractUserDev")
        }

        afterEvaluate {
            // ensure mod jar output centralizes
            tasks.withType<Jar>().configureEach {
                destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
            }
        }
    }
}

// ───────────────────────────────────────────────
// Build aggregation
// ───────────────────────────────────────────────
val primary = versionDirs.first().name
tasks.register("buildApp") {
    group = "build"
    dependsOn(":$primary:build")
}
tasks.register("buildAllApp") {
    group = "build"
    dependsOn(versionDirs.map { ":${it.name}:build" })
}
tasks.register("allClean") {
    group = "build"
    doFirst {
        val libsDir = rootProject.layout.buildDirectory.dir("libs").get().asFile
        libsDir.listFiles()?.forEach { it.delete() }
    }
    dependsOn(versionDirs.map { ":${it.name}:clean" })
}

// ───────────────────────────────────────────────
// Jar relocation
// ───────────────────────────────────────────────
gradle.projectsEvaluated {
    allprojects.forEach { sub ->
        sub.tasks.withType<Jar>().configureEach {
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
            doLast {
                val mcVer = sub.findProperty("MC_VERSION")?.toString() ?: "unknown"
                val modVer = sub.findProperty("MOD_VERSION")?.toString() ?: "dev"
                val destDir = destinationDirectory.get().asFile
                val src = archiveFile.get().asFile
                if (src.exists()) {
                    val newName = "TheExpanse-NeoForge+$mcVer-$modVer.jar"
                    val dst = File(destDir, newName)
                    destDir.listFiles()?.filter { it.name.contains("TheExpanse-NeoForge+$mcVer") }?.forEach { it.delete() }
                    src.renameTo(dst)
                    println("✅ Relocated ${sub.name} jar → ${dst.name}")
                }
            }
        }
    }
}

// ───────────────────────────────────────────────
// Prevent directory mod issue
// ───────────────────────────────────────────────
afterEvaluate {
    allprojects.forEach { sub ->
        sub.extensions.findByName("runs")?.let {
            @Suppress("UNCHECKED_CAST")
            val runs = it as NamedDomainObjectContainer<Run>
            runs.configureEach {
                modSources { sourceSets.removeIf { s -> s.name == "main" } }
            }
        }
    }
}
