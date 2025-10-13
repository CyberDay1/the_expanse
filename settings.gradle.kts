rootProject.name = "the_expanse"

file("versions").listFiles()
    ?.filter { it.isDirectory }
    ?.forEach { include(":${it.name}") }
