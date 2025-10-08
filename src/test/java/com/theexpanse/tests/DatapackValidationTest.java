package com.theexpanse.tests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatapackValidationTest {
    private static final Path PROJECT_ROOT = locateProjectRoot();
    private static final Path BASE_RESOURCES = PROJECT_ROOT.resolve("src/main/resources");
    private static final Path VERSIONS_DIR = PROJECT_ROOT.resolve("versions");

    private static Path worldgenRelativeRoot;
    private static Path dimensionTypeRelativeRoot;

    @BeforeAll
    static void resolveRoots() throws IOException {
        worldgenRelativeRoot = selectDataRoot(BASE_RESOURCES,
            Path.of("data", "the_expanse", "worldgen"),
            Path.of("data", "minecraft", "worldgen"));
        dimensionTypeRelativeRoot = selectDataRoot(BASE_RESOURCES,
            Path.of("data", "the_expanse", "dimension_type"),
            Path.of("data", "minecraft", "dimension_type"));

        Path worldgenAbsolute = BASE_RESOURCES.resolve(worldgenRelativeRoot);
        assertTrue(Files.isDirectory(worldgenAbsolute),
            "Worldgen root " + worldgenRelativeRoot + " does not exist");
        assertFalse(collectJsonFiles(worldgenAbsolute).isEmpty(),
            "No worldgen JSON files were discovered under " + worldgenRelativeRoot + ".");

        Path dimensionAbsolute = BASE_RESOURCES.resolve(dimensionTypeRelativeRoot);
        assertTrue(Files.isDirectory(dimensionAbsolute),
            "Dimension type root " + dimensionTypeRelativeRoot + " does not exist");
        assertFalse(collectJsonFiles(dimensionAbsolute).isEmpty(),
            "No dimension type JSON files were discovered under " + dimensionTypeRelativeRoot + ".");
    }

    @Test
    void worldgenDefinitionsAreConsistentAcrossVariants() throws IOException {
        Map<String, JsonElement> baseline = loadMergedJson(BASE_RESOURCES, null, worldgenRelativeRoot);
        assertFalse(baseline.isEmpty(), "Baseline worldgen dataset should not be empty");

        for (String variant : listVariants()) {
            Path variantResources = resolveVariantResources(variant);
            Map<String, JsonElement> candidate = loadMergedJson(BASE_RESOURCES, variantResources, worldgenRelativeRoot);

            assertEquals(baseline.keySet(), candidate.keySet(),
                () -> "Worldgen keys diverged for variant " + variant);
            baseline.forEach((key, value) -> assertEquals(value, candidate.get(key),
                "Worldgen definition mismatch for " + key + " in variant " + variant));
        }
    }

    @Test
    void dimensionTypesRespectVerticalBounds() throws IOException {
        Map<String, JsonElement> baseline = loadMergedJson(BASE_RESOURCES, null, dimensionTypeRelativeRoot);
        assertFalse(baseline.isEmpty(), "Dimension type dataset should not be empty");

        for (Map.Entry<String, JsonElement> entry : baseline.entrySet()) {
            JsonObject dimension = entry.getValue().getAsJsonObject();
            assertEquals(-256, dimension.get("min_y").getAsInt(),
                () -> "min_y mismatch in " + entry.getKey());
            assertEquals(2288, dimension.get("height").getAsInt(),
                () -> "height mismatch in " + entry.getKey());
            if (dimension.has("logical_height")) {
                assertEquals(2288, dimension.get("logical_height").getAsInt(),
                    () -> "logical_height mismatch in " + entry.getKey());
            }
        }

        for (String variant : listVariants()) {
            Path variantResources = resolveVariantResources(variant);
            Map<String, JsonElement> candidate = loadMergedJson(
                BASE_RESOURCES, variantResources, dimensionTypeRelativeRoot);
            assertEquals(baseline, candidate, () -> "Dimension type definitions diverged for " + variant);
        }
    }

    @Test
    void noiseSettingsStayWithinExpectedRange() throws IOException {
        Map<String, JsonElement> baseline = loadMergedJson(BASE_RESOURCES, null, worldgenRelativeRoot);
        Collection<Map.Entry<String, JsonElement>> noiseSettings = baseline.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("noise_settings/"))
            .collect(Collectors.toList());

        assertFalse(noiseSettings.isEmpty(), "No noise_settings definitions discovered under " + worldgenRelativeRoot);

        for (Map.Entry<String, JsonElement> entry : noiseSettings) {
            JsonObject root = entry.getValue().getAsJsonObject();
            JsonObject noise = root.getAsJsonObject("noise");
            assertEquals(-256, noise.get("min_y").getAsInt(),
                () -> "noise.min_y mismatch in " + entry.getKey());
            assertEquals(2288, noise.get("height").getAsInt(),
                () -> "noise.height mismatch in " + entry.getKey());
        }

        for (String variant : listVariants()) {
            Path variantResources = resolveVariantResources(variant);
            Map<String, JsonElement> candidate = loadMergedJson(BASE_RESOURCES, variantResources, worldgenRelativeRoot);
            Collection<Map.Entry<String, JsonElement>> candidateNoise = candidate.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("noise_settings/"))
                .collect(Collectors.toList());
            assertEquals(noiseSettings.size(), candidateNoise.size(),
                () -> "Noise settings count mismatch for " + variant);
            for (Map.Entry<String, JsonElement> entry : noiseSettings) {
                assertEquals(entry.getValue(), candidate.get(entry.getKey()),
                    "Noise settings diverged for " + entry.getKey() + " in variant " + variant);
            }
        }
    }

    @Test
    void oreScalingPlacementsDeclareModifiers() throws IOException {
        Map<String, JsonElement> baseline = loadMergedJson(BASE_RESOURCES, null, worldgenRelativeRoot);
        List<Map.Entry<String, JsonElement>> oreScalingPlaced = baseline.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("placed_feature/"))
            .filter(entry -> entry.getKey().contains("ore_scaling"))
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toCollection(ArrayList::new));

        Assumptions.assumeFalse(oreScalingPlaced.isEmpty(),
            "No ore_scaling placed features present under " + worldgenRelativeRoot
                + ". Add datapack entries to enable this validation.");

        for (Map.Entry<String, JsonElement> entry : oreScalingPlaced) {
            JsonObject placed = entry.getValue().getAsJsonObject();
            assertTrue(placed.has("placement"),
                () -> "Missing placement array for " + entry.getKey());
            assertTrue(placed.get("placement").isJsonArray(),
                () -> "Placement element is not an array for " + entry.getKey());
            assertTrue(placed.getAsJsonArray("placement").size() > 0,
                () -> "Placement array is empty for " + entry.getKey());
        }

        for (String variant : listVariants()) {
            Path variantResources = resolveVariantResources(variant);
            Map<String, JsonElement> candidate = loadMergedJson(BASE_RESOURCES, variantResources, worldgenRelativeRoot);
            for (Map.Entry<String, JsonElement> entry : oreScalingPlaced) {
                assertEquals(entry.getValue(), candidate.get(entry.getKey()),
                    "Ore scaling placement mismatch for " + entry.getKey() + " in variant " + variant);
            }
        }
    }

    private static Path resolveVariantResources(String variant) {
        Path variantRoot = VERSIONS_DIR.resolve(variant).resolve("src/main/resources");
        return Files.isDirectory(variantRoot) ? variantRoot : null;
    }

    private static List<String> listVariants() throws IOException {
        if (!Files.isDirectory(VERSIONS_DIR)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(VERSIONS_DIR)) {
            return stream
                .filter(Files::isDirectory)
                .map(path -> path.getFileName().toString())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
    }

    private static Map<String, JsonElement> loadMergedJson(Path baseResources,
                                                           Path variantResources,
                                                           Path relativeRoot) throws IOException {
        Map<String, JsonElement> merged = new TreeMap<>();
        Path baseRoot = baseResources.resolve(relativeRoot);
        merged.putAll(readJsonTree(baseRoot, baseRoot));

        if (variantResources != null) {
            Path variantRoot = variantResources.resolve(relativeRoot);
            merged.putAll(readJsonTree(variantRoot, variantRoot));
        }

        return merged;
    }

    private static Map<String, JsonElement> readJsonTree(Path root, Path relativizeBase) throws IOException {
        if (!Files.isDirectory(root)) {
            return Map.of();
        }

        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toMap(
                    path -> relativizeBase.relativize(path).toString().replace('\\', '/'),
                    DatapackValidationTest::parseJson,
                    (existing, replacement) -> replacement,
                    TreeMap::new
                ));
        }
    }

    private static JsonElement parseJson(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read JSON from " + path, exception);
        }
    }

    private static Path locateProjectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to resolve project root from working directory");
    }

    private static Path selectDataRoot(Path resourcesRoot, Path primary, Path fallback) throws IOException {
        if (hasJsonFiles(resourcesRoot.resolve(primary))) {
            return primary;
        }
        if (hasJsonFiles(resourcesRoot.resolve(fallback))) {
            return fallback;
        }
        return primary;
    }

    private static boolean hasJsonFiles(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return false;
        }
        return !collectJsonFiles(root).isEmpty();
    }

    private static List<Path> collectJsonFiles(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toList());
        }
    }
}
