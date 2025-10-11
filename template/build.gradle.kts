import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java")
    id("net.neoforged.gradle") version "6.0.18"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

group = "com.cyberday"
version = "1.0.0"

minecraft {
    mappings("official", stonecutter["MC_VERSION"])
}

dependencies {
    implementation("net.neoforged:neoforge:${stonecutter["NEOFORGE"]}")
}

afterEvaluate {
    val mcVersion = project.name.substringBefore("-")
    tasks.withType<Jar>().configureEach {
        archiveBaseName.set("the_expanse")
        archiveVersion.set(mcVersion)
        destinationDirectory.set(layout.buildDirectory.dir("libs/$mcVersion"))
    }
    version = mcVersion
}
