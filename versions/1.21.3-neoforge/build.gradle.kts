plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.190"
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

base {
    archivesName.set("TheExpanse-NeoForge")
}

group = "com.theexpanse"
version = project.property("MOD_VERSION")!!

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

dependencies {
    implementation("net.neoforged:neoforge:${property("NEOFORGE_VERSION")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.processResources {
    val modVersion = property("MOD_VERSION")
    val mcVersion = property("MC_VERSION")
    val neoVersion = property("NEOFORGE_VERSION")
    val packFormat = property("PACK_FORMAT")

    inputs.properties(
        mapOf(
            "modVersion" to modVersion,
            "mcVersion" to mcVersion,
            "neoVersion" to neoVersion,
            "packFormat" to packFormat
        )
    )

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(
            mapOf(
                "MOD_VERSION" to modVersion,
                "MC_VERSION" to mcVersion,
                "NEOFORGE_VERSION" to neoVersion,
                "PACK_FORMAT" to packFormat
            )
        )
    }
}

tasks.jar {
    archiveBaseName.set("TheExpanse-NeoForge")
    archiveVersion.set("${property("MC_VERSION")}-${property("MOD_VERSION")}")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"])
            groupId = "com.theexpanse"
            artifactId = "theexpanse"
            version = project.version.toString()
        }
    }
}

runs {
    create("client") {
        workingDirectory.set(file("run/client"))
        ideaModule.set("${project.name}.main")
        taskName.set("Client")
        property("forge.logging.console.level", "info")
        jvmArgs("-Xmx4G")
    }

    create("server") {
        workingDirectory.set(file("run/server"))
        ideaModule.set("${project.name}.main")
        taskName.set("Server")
        args("--nogui")
    }
}


}

val reobfJar = tasks.findByName("reobfJar") ?: tasks.register("reobfJar") {
    dependsOn(tasks.jar)
}

tasks.register("buildMod") {
    group = "build"
    description = "Builds the distributable mod jar"
    dependsOn(tasks.jar, reobfJar)
}
