plugins {
    id("java")
    id("net.neoforged.moddev") version "1.+"
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

group = "com.theexpanse"
version = properties["mod.version"]!!
base.archivesName.set("the_expanse")

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation("net.neoforged:neoforge:${properties["NEOFORGE_VERSION"]}")
}

tasks.jar {
    from("src/main/resources")
    manifest {
        attributes(
            "Specification-Title" to "the_expanse",
            "Specification-Vendor" to "CyberDay1",
            "Specification-Version" to project.version,
            "Implementation-Title" to "the_expanse",
            "Implementation-Version" to project.version
        )
    }
}
