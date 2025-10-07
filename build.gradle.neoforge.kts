plugins {
    id("dev.isxander.modstitch") version "0.5.15-unstable"
}

modstitch {
    loader.set("neoforge")
    minecraft.set(providers.gradleProperty("mcVersion"))
    modVersion.set(providers.gradleProperty("modVersion"))
    group.set("com.theexpanse")
    archivesName.set("the_expanse")
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    // NeoForge userdev is injected by modstitch
}

tasks.jar {
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

val reobfJar = tasks.findByName("reobfJar")

tasks.register("buildMod") {
    group = "build"
    description = "Assembles and reobfuscates the NeoForge mod jar"
    if (reobfJar != null) {
        dependsOn(reobfJar)
    } else {
        dependsOn(tasks.build)
    }
}
