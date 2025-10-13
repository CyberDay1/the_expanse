gradle.afterProject {
    if (project == rootProject) {
        val targets = subprojects.mapNotNull { p ->
            when {
                p.tasks.findByName("build") != null   -> "${p.path}:build"
                p.tasks.findByName("assemble") != null -> "${p.path}:assemble"
                else -> null
            }
        }

        tasks.register("buildAll") {
            group = "build"
            description = "Builds all Stonecutter version variants."
            dependsOn(targets)
        }

        tasks.register("cleanAll") {
            group = "build"
            description = "Cleans all Stonecutter version variants."
            dependsOn(subprojects.mapNotNull { p ->
                if (p.tasks.findByName("clean") != null) "${p.path}:clean" else null
            })
        }
    }
}
