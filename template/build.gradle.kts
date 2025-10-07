import org.gradle.api.tasks.JavaExec
plugins {
    id("java")
}

group = "com.theexpanse"
version = "0.1.0"
base {
    archivesName.set("the_expanse")
}

repositories {
    mavenCentral()
}


val datapackValidationTest by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Validates Patchouli cross-link metadata for JEI HUD overlays."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.theexpanse.datapack.DatapackValidationTest")
    workingDir = project.projectDir
}

tasks.named("check") {
    dependsOn(datapackValidationTest)
}
