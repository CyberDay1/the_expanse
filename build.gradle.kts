plugins {
    id("base")
}
allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.neoforged.net/releases") }
    }
}

