package com.worldrise;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

class WorldriseResourceValidationTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("mods.toml parses and declares the worldrise modId")
    void modsTomlParses() throws IOException {
        Path modsToml = RESOURCE_ROOT.resolve(Path.of("META-INF", "mods.toml"));
        assertTrue(Files.exists(modsToml), "mods.toml should exist");

        TomlParseResult result = Toml.parse(modsToml);
        assertTrue(result.errors().isEmpty(), () ->
                "mods.toml had parse errors: " + result.errors());

        assertEquals("worldrise", result.getString("modId"),
                "mods.toml should declare the worldrise modId");
    }

    @Test
    @DisplayName("pack.mcmeta is valid JSON")
    void packMcmetaParses() throws IOException {
        Path pack = RESOURCE_ROOT.resolve("pack.mcmeta");
        assertTrue(Files.exists(pack), "pack.mcmeta should exist");

        JsonNode node = MAPPER.readTree(Files.newBufferedReader(pack));
        assertNotNull(node);
        assertTrue(node.has("pack"), "pack.mcmeta should contain a pack object");
    }

    @Test
    @DisplayName("All resource JSON files parse without exceptions")
    void allResourceJsonParses() throws IOException {
        try (Stream<Path> paths = Files.walk(RESOURCE_ROOT)) {
            List<Path> jsonFiles = paths
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                    .collect(Collectors.toList());

            assertFalse(jsonFiles.isEmpty(), "Expected resource JSON files to validate");

            for (Path jsonFile : jsonFiles) {
                assertDoesNotThrow(() -> MAPPER.readTree(Files.newBufferedReader(jsonFile)),
                        () -> "Failed to parse JSON file: " + RESOURCE_ROOT.relativize(jsonFile));
            }
        }
    }
}
