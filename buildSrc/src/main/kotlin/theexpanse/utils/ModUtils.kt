package theexpanse.utils

import org.gradle.api.Project

fun Project.mod(key: String): String =
    (findProperty("mod.\") ?: error("Missing property mod.\")).toString()

fun Project.deps(key: String): String =
    (findProperty("deps.\") ?: error("Missing dependency \")).toString()
