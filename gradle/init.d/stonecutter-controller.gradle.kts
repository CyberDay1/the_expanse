stonecutter {
    create(rootProject) {
        centralScript.set("stonecutter.gradle.kts")
        vcsVersion.set("1.21.1")
        kotlinController.set(true)
        versions(
            "1.21.1",
            "1.21.2",
            "1.21.3",
            "1.21.4",
            "1.21.5",
            "1.21.6",
            "1.21.7",
            "1.21.8",
            "1.21.9",
            "1.21.10"
        )
    }
}
