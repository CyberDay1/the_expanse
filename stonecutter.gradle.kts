plugins {
    id("dev.kayla.stonecutter")
}

val activeVariant = providers.fileContents(layout.projectDirectory.file("stonecutter.json"))
    .asText
    .map { text ->
        "\"default\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            .find(text)
            ?.groupValues
            ?.get(1)
            ?: error("stonecutter.json must define a default variant")
    }

stonecutter active activeVariant.get()

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "build"
    ofTask("build")
}
