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

    @Test
    @DisplayName("Ocean canyon carver geometry matches expected values")
    void oceanCanyonCarverMatchesSpec() throws IOException {
        Path carver = RESOURCE_ROOT.resolve(Path.of("data", "worldrise", "worldgen",
                "configured_carver", "ocean_canyon.json"));
        assertTrue(Files.exists(carver), "ocean_canyon.json should exist");

        JsonNode root = MAPPER.readTree(Files.newBufferedReader(carver));
        JsonNode config = root.get("config");
        assertNotNull(config, "Configured carver must declare a config object");

        assertEquals(1.5, config.get("horizontal_radius_multiplier").asDouble(),
                1.0E-6, "Horizontal radius multiplier should widen ravines to 1.5x");

        JsonNode yNode = config.get("y");
        assertNotNull(yNode, "Configured carver must define a y distribution");
        assertEquals("minecraft:uniform", yNode.get("type").asText(),
                "Ocean canyon carver should use a uniform Y distribution");

        JsonNode valueNode = yNode.get("value");
        assertNotNull(valueNode, "Uniform distribution must define value bounds");
        assertEquals(-50, valueNode.get("min_inclusive").asInt(),
                "Canyon floor should start at Y = -50");
        assertEquals(-30, valueNode.get("max_inclusive").asInt(),
                "Canyon roof should top out at Y = -30");
    }

    @Test
    @DisplayName("Ocean-like biome tag references vanilla oceans")
    void oceanLikeBiomeTagResolves() throws IOException {
        Path tag = RESOURCE_ROOT.resolve(Path.of("data", "worldrise", "tags", "worldgen",
                "biome", "ocean_like.json"));
        assertTrue(Files.exists(tag), "ocean_like biome tag should exist");

        JsonNode root = MAPPER.readTree(Files.newBufferedReader(tag));
        assertTrue(root.has("values"), "ocean_like tag must declare biome values");
        JsonNode values = root.get("values");
        assertTrue(values.isArray(), "ocean_like values should be an array");

        List<String> biomes = Stream.of("minecraft:ocean", "minecraft:deep_ocean",
                        "minecraft:warm_ocean", "minecraft:lukewarm_ocean",
                        "minecraft:cold_ocean", "minecraft:frozen_ocean")
                .collect(Collectors.toList());

        for (String biome : biomes) {
            boolean present = false;
            for (JsonNode value : values) {
                if (biome.equals(value.asText())) {
                    present = true;
                    break;
                }
            }
            assertTrue(present, () -> biome + " should be included in ocean_like tag");
        }
    }
}
