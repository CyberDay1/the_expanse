package com.theexpanse;

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
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

class TheExpanseResourceValidationTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("mods.toml parses and declares the the_expanse modId")
    void modsTomlParses() throws IOException {
        Path modsToml = RESOURCE_ROOT.resolve(Path.of("META-INF", "mods.toml"));
        assertTrue(Files.exists(modsToml), "mods.toml should exist");

        TomlParseResult result = Toml.parse(modsToml);
        assertTrue(result.errors().isEmpty(), () ->
                "mods.toml had parse errors: " + result.errors());

        TomlArray mods = result.getArray("mods");
        assertNotNull(mods, "mods.toml should declare at least one mod entry");
        assertTrue(mods.size() > 0, "mods.toml should list the the_expanse mod");

        TomlTable modEntry = mods.getTable(0);
        assertNotNull(modEntry, "mods.toml should contain a table for the the_expanse mod");

        assertEquals("the_expanse", modEntry.getString("modId"),
                "mods.toml should declare the the_expanse modId");
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
        Path carver = RESOURCE_ROOT.resolve(Path.of("data", "the_expanse", "worldgen",
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
    @DisplayName("Mega ravine carver uses expanded vertical range")
    void megaRavineCarverMatchesSpec() throws IOException {
        Path carver = RESOURCE_ROOT.resolve(Path.of("data", "the_expanse", "worldgen",
                "configured_carver", "mega_ravine.json"));
        assertTrue(Files.exists(carver), "mega_ravine.json should exist");

        JsonNode root = MAPPER.readTree(Files.newBufferedReader(carver));
        JsonNode config = root.get("config");
        assertNotNull(config, "Mega ravine must define a config object");

        assertEquals(0.02, config.get("probability").asDouble(), 1.0E-6,
                "Mega ravine probability should be 0.02");
        JsonNode yNode = config.get("y");
        assertNotNull(yNode, "Mega ravine carver must define a y distribution");
        assertEquals(-128, yNode.get("min_inclusive").get("absolute").asInt(),
                "Mega ravines should reach down to Y = -128");
        assertEquals(64, yNode.get("max_inclusive").get("absolute").asInt(),
                "Mega ravines should extend up to Y = 64");
        assertEquals(2.5, config.get("horizontal_radius_multiplier").asDouble(), 1.0E-6,
                "Mega ravines should be 2.5x wider");
        assertEquals(1.2, config.get("vertical_radius_multiplier").asDouble(), 1.0E-6,
                "Mega ravines should be slightly taller");
    }

    @Test
    @DisplayName("Surface sinkhole carver targets high Y values")
    void sinkholeCarverMatchesSpec() throws IOException {
        Path carver = RESOURCE_ROOT.resolve(Path.of("data", "the_expanse", "worldgen",
                "configured_carver", "sinkhole.json"));
        assertTrue(Files.exists(carver), "sinkhole.json should exist");

        JsonNode root = MAPPER.readTree(Files.newBufferedReader(carver));
        JsonNode config = root.get("config");
        assertNotNull(config, "Sinkhole carver must define a config object");

        assertEquals(0.01, config.get("probability").asDouble(), 1.0E-6,
                "Sinkhole probability should be 0.01");
        JsonNode yNode = config.get("y");
        assertNotNull(yNode, "Sinkhole carver must define a y distribution");
        assertEquals(60, yNode.get("min_inclusive").get("absolute").asInt(),
                "Sinkholes should begin close to the surface");
        assertEquals(200, yNode.get("max_inclusive").get("absolute").asInt(),
                "Sinkholes should extend above sea level");
        assertEquals(2.0, config.get("horizontal_radius_multiplier").asDouble(), 1.0E-6,
                "Sinkholes should be roughly twice as wide");
        assertEquals(2.5, config.get("vertical_radius_multiplier").asDouble(), 1.0E-6,
                "Sinkholes should be tall columns");
    }

    @Test
    @DisplayName("Ocean blue hole carver stays within underwater range")
    void oceanBlueHoleCarverMatchesSpec() throws IOException {
        Path carver = RESOURCE_ROOT.resolve(Path.of("data", "the_expanse", "worldgen",
                "configured_carver", "ocean_blue_hole.json"));
        assertTrue(Files.exists(carver), "ocean_blue_hole.json should exist");

        JsonNode root = MAPPER.readTree(Files.newBufferedReader(carver));
        JsonNode config = root.get("config");
        assertNotNull(config, "Ocean blue hole carver must define a config object");

        assertEquals(0.01, config.get("probability").asDouble(), 1.0E-6,
                "Ocean blue hole probability should be 0.01");
        JsonNode yNode = config.get("y");
        assertNotNull(yNode, "Ocean blue hole carver must define a y distribution");
        assertEquals(-40, yNode.get("min_inclusive").get("absolute").asInt(),
                "Blue holes should start below the ocean floor");
        assertEquals(-10, yNode.get("max_inclusive").get("absolute").asInt(),
                "Blue holes should rise to Y = -10");
        assertEquals(3.0, config.get("horizontal_radius_multiplier").asDouble(), 1.0E-6,
                "Blue holes should be very wide");
        assertEquals(3.0, config.get("vertical_radius_multiplier").asDouble(), 1.0E-6,
                "Blue holes should be very tall shafts");
    }

    @Test
    @DisplayName("Ocean-like biome tag references vanilla oceans")
    void oceanLikeBiomeTagResolves() throws IOException {
        Path tag = RESOURCE_ROOT.resolve(Path.of("data", "the_expanse", "tags", "worldgen",
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
