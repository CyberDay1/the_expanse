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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.register("verifyDependencyLocks") {
    group = "verification"
    description = "Ensures dependency lockfiles are up-to-date and honored."
    doLast {
        configurations
            .filter { it.isCanBeResolved }
            .forEach { it.resolve() }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

val configurableProperties = Properties().apply {
    val configFile = rootProject.file("configurable.properties")
    if (configFile.exists()) {
        configFile.inputStream().use { load(it) }
    }
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

val datapackRuntimeRun = runs.register("datapackRuntime") {
    run("server")
    arguments.add("--nogui")
    shouldExportToIDE(false)
    workingDirectory(datapackRuntimeRunDir.get().asFile)
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("com.google.code.gson:gson:2.11.0")
}

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
        ktlint().editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "continuation_indent_size" to "4"
            )
        )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target(
            "*.gradle.kts",
            "../*.gradle.kts",
            "buildSrc/**/*.gradle.kts",
            "template/**/*.gradle.kts",
            "versions/**/*.gradle.kts"
        )
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint().editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "continuation_indent_size" to "4"
            )
        )
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
        txt.required.set(false)
        xml.required.set(true)
        sarif.required.set(false)
    }
}

tasks.register<Detekt>("detektMain") {
    description = "Runs Detekt analysis on the main Kotlin sources."
    group = "verification"
    buildUponDefaultConfig = true
    config.setFrom(detektConfig)
    setSource(
        files(
            "template/src/main/kotlin",
            rootProject.layout.projectDirectory.dir("template/src/main/kotlin")
        )
    )
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/.gradle/**")
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Checkstyle>("checkstyleMain") {
    setDependsOn(emptyList<Any>())
    classpath = files()
    val sourceSets = extensions.findByType<org.gradle.api.tasks.SourceSetContainer>()
    source = sourceSets?.named("main")?.get()?.allJava ?: fileTree(rootProject.layout.projectDirectory.dir("template/src/main/java")) {
        include("**/*.java")
        exclude("**/build/**", "**/.gradle/**")
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck", "checkstyleMain", "detektMain")
}

val datapackRuntimeLog = layout.buildDirectory.file("datapackRuntime/server/logs/datapack-runtime.log")

tasks.register("datapackRuntimeTest") {
    group = "verification"
    description = "Starts a headless NeoForge server to validate bundled datapacks."
    dependsOn("build", "testClasses", "writeMinecraftClasspathServer", "writeMinecraftClasspathDatapackRuntime")
    outputs.file(datapackRuntimeLog)

    doLast {
        val runTask = project.tasks.named<JavaExec>("runDatapackRuntime").get()
        val runDir = datapackRuntimeRunDir.get().asFile

        if (runDir.exists()) {
            runDir.deleteRecursively()
        }
        runDir.mkdirs()

        val logsDir = File(runDir, "logs")
        logsDir.mkdirs()
        val logFile = datapackRuntimeLog.get().asFile

        val eulaFile = File(runDir, "eula.txt")
        eulaFile.writeText("eula=true\n")

        val serverProperties = File(runDir, "server.properties")
        serverProperties.writeText(
            """
                allow-flight=true
                difficulty=peaceful
                enable-command-block=false
                enforce-secure-profile=false
                gamemode=creative
                level-name=datapack_runtime
                max-players=1
                motd=The Expanse Datapack Validation
                online-mode=false
                simulation-distance=4
                spawn-animals=false
                spawn-monsters=false
                view-distance=4
            """.trimIndent() + "\n"
        )

        val javaExecutableName = if (System.getProperty("os.name").lowercase().contains("win")) "java.exe" else "java"
        val javaExecutable = runTask.javaLauncher.orNull?.executablePath?.asFile?.absolutePath
            ?: runTask.executable
            ?: File(System.getProperty("java.home"), "bin/$javaExecutableName").absolutePath

        val command = mutableListOf<String>()
        command += javaExecutable
        command.addAll(runTask.allJvmArgs)
        command.addAll(
            listOf(
                "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED"
            )
        )
        command += listOf("-cp", runTask.classpath.asPath)
        command += runTask.mainClass.get()
        runTask.args?.let { command.addAll(it) }

        logger.lifecycle("Datapack runtime JVM args: {}", runTask.allJvmArgs.joinToString(" "))
        logger.lifecycle("Datapack runtime command: {}", command.joinToString(" ").take(500))

        val processBuilder = ProcessBuilder(command)
            .directory(runTask.workingDir ?: runDir)
            .redirectErrorStream(true)

        val environment = processBuilder.environment()
        environment.putAll(runTask.environment.mapValues { it.value?.toString() ?: "" })

        val process = processBuilder.start()
        val stdinWriter = process.outputStream.bufferedWriter()
        val stopSent = AtomicBoolean(false)
        val failureDetected = AtomicBoolean(false)
        val datapackFailureDetected = AtomicBoolean(false)

        val readerExecutor = Executors.newSingleThreadExecutor()
        val readerFuture = readerExecutor.submit<Unit> {
            logFile.bufferedWriter().use { writer ->
                process.inputStream.bufferedReader().use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        writer.appendLine(line)
                        writer.flush()

                        val normalized = line.lowercase()
                        if (!stopSent.get() && normalized.contains("done") && normalized.contains("for help")) {
                            synchronized(stdinWriter) {
                                stdinWriter.write("stop\n")
                                stdinWriter.flush()
                            }
                            stopSent.set(true)
                        }

                        val isIgnorableAuthlibError = normalized.contains("yggdrasilserviceskeyinfo")

                        if ((normalized.contains("[error]") ||
                            normalized.contains("/error]") ||
                            normalized.contains("encountered an unexpected exception") ||
                            normalized.contains("caught exception") ||
                            normalized.contains("fatal") ||
                            normalized.contains("missing required registry")) &&
                            !isIgnorableAuthlibError
                        ) {
                            failureDetected.set(true)
                        }

                        if (normalized.contains("failed to reload data packs") ||
                            normalized.contains("errors in currently selected datapacks") ||
                            normalized.contains("couldn't load data pack") ||
                            normalized.contains("invalid datapack")
                        ) {
                            datapackFailureDetected.set(true)
                        }
                    }
                }
            }
        }

        val finished = process.waitFor(180, TimeUnit.SECONDS)
        if (!finished) {
            if (!stopSent.get()) {
                synchronized(stdinWriter) {
                    stdinWriter.write("stop\n")
                    stdinWriter.flush()
                }
                stopSent.set(true)
            }
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly()
            }
        }

        readerFuture.get()
        readerExecutor.shutdown()
        readerExecutor.awaitTermination(30, TimeUnit.SECONDS)
        stdinWriter.close()

        val exitCode = process.exitValue()
        if (!finished || exitCode != 0) {
            throw GradleException("Datapack runtime server exited abnormally (code $exitCode). See log at ${logFile.absolutePath}.")
        }

        if (datapackFailureDetected.get()) {
            throw GradleException("Datapack runtime validation reported datapack loading failures. See log at ${logFile.absolutePath}.")
        }

        if (failureDetected.get()) {
            throw GradleException("Datapack runtime validation detected server errors. See log at ${logFile.absolutePath}.")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val ciBuild = providers.environmentVariable("CI").map { it.equals("true", ignoreCase = true) }.orElse(false)

tasks.withType<JavaCompile>().configureEach {
    if (ciBuild.orNull == true && !options.compilerArgs.contains("-Werror")) {
        options.compilerArgs.add("-Werror")
    }
}

// Expand tokens in resources (neoforge.mods.toml, pack.mcmeta, etc.)
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
        }
    }
}

if (!enableDatagen) {
    tasks.configureEach {
        if (name.contains("datagen", ignoreCase = true)) {
            enabled = false
        }
    }
}

if (!useMixins) {
    tasks.configureEach {
        if (name.contains("mixin", ignoreCase = true)) {
            enabled = false
        }
    }
}

val shouldDownloadAssets = gradle.startParameter.taskNames.any { it.contains("datapackRuntimeTest") }

tasks.configureEach {
    if (name.endsWith("DownloadAssets")) {
        enabled = shouldDownloadAssets
    }
}
