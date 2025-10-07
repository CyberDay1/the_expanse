plugins {
    id("java")
    id("base")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "6.0.+"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

group = "com.theexpanse"
version = providers.gradleProperty("modVersion").getOrElse("0.1.0")

base.archivesName.set("the_expanse")

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
}

dependencies {
    minecraft("net.minecraftforge:forge:${providers.gradleProperty("mcVersion").get()}-${providers.gradleProperty("loaderVersion").get()}")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from("src/main/resources")
    manifest {
        attributes(
            "Specification-Title" to "the_expanse",
            "Specification-Vendor" to "CyberDay1",
            "Specification-Version" to project.version,
            "Implementation-Title" to "the_expanse",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "CyberDay1"
        )
    }
}

// ForgeGradle wires reobf automatically in most setups; fall back if absent.
val reobfJar = tasks.findByName("reobfJar") ?: tasks.register("reobfJar") {
    dependsOn(tasks.jar)
}

tasks.named("build") {
    dependsOn(tasks.jar, reobfJar)
}

tasks.register("buildMod") {
    group = "build"
    description = "Builds distributable mod jar (includes reobf when available)"
    dependsOn("build")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])
        }
    }
}
