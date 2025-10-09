package com.theexpanse.parity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackMetadataParityTest {
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^\\s*version\\s*=\\s*\"([^\"]+)\"",
        Pattern.MULTILINE
    );

    @Test
    void packMetadataTokensExpandWithVariantValues() throws IOException {
        Optional<String> activeVariant = ParityTestSupport.detectActiveVariant();
        Assumptions.assumeTrue(activeVariant.isPresent(),
            "Unable to detect active Stonecutter variant from execution context");

        String variant = activeVariant.get();
        Properties properties = ParityTestSupport.loadVariantProperties(variant);
        Path buildRoot = Optional.ofNullable(ParityTestSupport.resolveVariantBuildResources(variant))
            .orElse(ParityTestSupport.resolveSharedBuildResources());
        Assumptions.assumeTrue(buildRoot != null,
            "No processed resources were generated for variant " + variant);

        validatePackMetadata(buildRoot.resolve("pack.mcmeta"), properties);
        validateModsToml(buildRoot.resolve("META-INF/neoforge.mods.toml"), properties);
    }

    private static void validatePackMetadata(Path packPath, Properties properties) throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(packPath),
            "pack.mcmeta was not generated at " + packPath);

        JsonElement root = JsonParser.parseString(Files.readString(packPath));
        assertTrue(root.isJsonObject(), "pack.mcmeta did not contain a JSON object");
        JsonObject pack = root.getAsJsonObject().getAsJsonObject("pack");
        assertNotNull(pack, "pack.mcmeta is missing the pack object");

        String packFormat = properties.getProperty("PACK_FORMAT");
        assertNotNull(packFormat, "PACK_FORMAT property is not defined for the active variant");
        assertEquals(Integer.parseInt(packFormat), pack.get("pack_format").getAsInt(),
            "pack_format did not match the variant configuration");

        String description = pack.get("description").getAsString();
        assertFalse(description.contains("${"), "Unexpanded token detected in pack description");

        String mcVersion = properties.getProperty("MC_VERSION");
        String neoVersion = properties.getProperty("NEOFORGE_VERSION");
        assertNotNull(mcVersion, "MC_VERSION property is not defined for the active variant");
        assertNotNull(neoVersion, "NEOFORGE_VERSION property is not defined for the active variant");
        assertTrue(description.contains(mcVersion), "pack description is missing the Minecraft version");
        assertTrue(description.contains(neoVersion), "pack description is missing the NeoForge version");
    }

    private static void validateModsToml(Path modsToml, Properties properties) throws IOException {
        Assumptions.assumeTrue(Files.isRegularFile(modsToml),
            "Processed neoforge.mods.toml not found at " + modsToml);

        String content = Files.readString(modsToml);
        assertFalse(content.contains("${"), "Unexpanded token detected in neoforge.mods.toml");

        Matcher versionMatcher = VERSION_PATTERN.matcher(content);
        assertTrue(versionMatcher.find(), "Version entry was not located in neoforge.mods.toml");
        String version = versionMatcher.group(1).trim();
        assertFalse(version.isBlank(), "Version in neoforge.mods.toml may not be blank");

        String mcVersion = properties.getProperty("MC_VERSION");
        String neoVersion = properties.getProperty("NEOFORGE_VERSION");
        assertNotNull(mcVersion, "MC_VERSION property is not defined for the active variant");
        assertNotNull(neoVersion, "NEOFORGE_VERSION property is not defined for the active variant");

        assertTrue(content.contains("versionRange = \"[" + mcVersion + "]\""),
            "Minecraft dependency range did not match variant value");
        assertTrue(content.contains("versionRange = \"[" + neoVersion + ",)\""),
            "NeoForge dependency range did not match variant value");
    }
}
