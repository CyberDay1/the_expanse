import dev.kikugie.stonecutter.Stonecutter

plugins {
    id("dev.kikugie.stonecutter")
}

Stonecutter.configure {
    // This creates subprojects for every entry in stonecutter.json
    registerAll()
}
