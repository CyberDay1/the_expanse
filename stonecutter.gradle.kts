import java.util.Properties

private const val DEFAULT_VARIANT = "1.21.1-neoforge"
private val SUPPORTED_VARIANTS = listOf(
    "1.21.1-neoforge",
    "1.21.2-neoforge",
    "1.21.3-neoforge",
    "1.21.4-neoforge",
    "1.21.5-neoforge",
    "1.21.6-neoforge",
    "1.21.7-neoforge",
    "1.21.8-neoforge",
    "1.21.9-neoforge",
)

private fun loadVariantProperties(name: String): Map<String, String> {
    val properties = Properties()
    val propertiesFile = settingsDir.resolve("versions/$name/gradle.properties")
    require(propertiesFile.isFile) { "Missing gradle.properties for variant '$name'" }
    propertiesFile.inputStream().use { properties.load(it) }
    return properties.entries.associate { (key, value) ->
        key.toString() to value.toString()
    }
}

stonecutter {
    active(DEFAULT_VARIANT)

    versions {
        SUPPORTED_VARIANTS.forEach { variant ->
            register(variant) {
                buildscript("build.neoforge.gradle.kts")
                loadVariantProperties(variant).forEach { (key, value) ->
                    extra(key, value)
                }
            }
        }
    }
}
