plugins {
    id("dev.isxander.modstitch") version "0.5.15-unstable"
}

modstitch {
    loader.set("forge")
    minecraft.set(providers.gradleProperty("mcVersion"))
    modVersion.set(providers.gradleProperty("modVersion"))
    group.set("com.theexpanse")
    archivesName.set("the_expanse")
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
}

dependencies {
    // Forge userdev is injected by modstitch
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
