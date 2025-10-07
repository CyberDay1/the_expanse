plugins {
    id 'java'
    id 'base' // provides clean
    id 'net.minecraftforge.gradle' version '6.0.+' // ForgeGradle
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven { url = 'https://maven.minecraftforge.net' }
}

minecraft {
    // ForgeGradle config
    mappings channel: 'official', version: project.mcVersion
}

dependencies {
    minecraft "net.minecraftforge:forge:${project.mcVersion}-${project.loaderVersion}"
}

tasks.named('jar') {
    from sourceSets.main.output
    archiveBaseName.set("the_expanse-${project.mcVersion}-${project.loader}")
}

artifacts {
    archives tasks.jar
}
