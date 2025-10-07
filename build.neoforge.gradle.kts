plugins {
    id("java")
    id("maven-publish")
    id("dev.kikugie.stonecutter") version "0.7.10"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

group = "com.theexpanse"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

stonecutter {
    // Bind properties so subprojects get MC + NeoForge version from stonecutter.json
    val mc: String by versionProperty
    val neoforge: String by versionProperty

    subprojects {
        apply(plugin = "net.neoforged.gradle.userdev")

        dependencies {
            "implementation"("net.neoforged:neoforge:$neoforge")
        }

        base {
            archivesName.set("the_expanse-$mc")
        }
    }
}
