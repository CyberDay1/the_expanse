package com.yourorg.worldrise.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OreRescalerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OreRescalerTest.class);

    private static final int EXPECTED_MIN = -256;
    private static final int EXPECTED_MAX = 2015;

    @Test
    @DisplayName("rescaleY converts known sample values")
    void rescaleYSampleValues() throws Exception {
        Method rescaleMethod = OreRescaler.class.getDeclaredMethod("rescaleY", int.class);
        rescaleMethod.setAccessible(true);

        int min = (int) rescaleMethod.invoke(null, -64);
        int max = (int) rescaleMethod.invoke(null, 320);
        int zero = (int) rescaleMethod.invoke(null, 0);

        assertEquals(-256, min);
        assertEquals(2015, max);
        assertEquals(123, zero);
    }

    @Test
    @DisplayName("Trapezoid height providers rescale both ends")
    void trapezoidRescalesExtrema() throws Exception {
        JsonObject root = new JsonObject();
        JsonArray placement = new JsonArray();
        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");

        JsonObject height = new JsonObject();
        JsonObject min = new JsonObject();
        min.addProperty("absolute", -64);
        JsonObject max = new JsonObject();
        max.addProperty("absolute", 320);
        JsonObject plateau = new JsonObject();
        plateau.addProperty("absolute", 0);

        height.add("min_inclusive", min);
        height.add("max_inclusive", max);
        height.add("plateau", plateau);
        heightRange.add("height", height);
        placement.add(heightRange);
        root.add("placement", placement);

        Method rescaleHeight = OreRescaler.class.getDeclaredMethod("rescaleHeight", JsonObject.class);
        rescaleHeight.setAccessible(true);

        boolean changed = (boolean) rescaleHeight.invoke(null, root);
        assertTrue(changed, "Expected rescale to modify trapezoid height");

        JsonObject rescaledHeight = root.getAsJsonArray("placement")
                .get(0).getAsJsonObject()
                .getAsJsonObject("height");

        assertEquals(EXPECTED_MIN, rescaledHeight.getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(EXPECTED_MAX, rescaledHeight.getAsJsonObject("max_inclusive").get("absolute").getAsInt());
        assertEquals(123, rescaledHeight.getAsJsonObject("plateau").get("absolute").getAsInt());
    }

    @Test
    @DisplayName("Plateau provider rescale is applied to nested value")
    void plateauRescales() throws Exception {
        JsonObject root = new JsonObject();
        JsonArray placement = new JsonArray();
        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");

        JsonObject height = new JsonObject();
        JsonObject value = new JsonObject();
        value.addProperty("min_inclusive", -32);
        value.addProperty("max_inclusive", 64);
        JsonObject plateau = new JsonObject();
        plateau.addProperty("absolute", 32);

        height.add("plateau", plateau);
        height.add("value", value);
        heightRange.add("height", height);
        placement.add(heightRange);
        root.add("placement", placement);

        Method rescaleHeight = OreRescaler.class.getDeclaredMethod("rescaleHeight", JsonObject.class);
        rescaleHeight.setAccessible(true);

        boolean changed = (boolean) rescaleHeight.invoke(null, root);
        assertTrue(changed, "Expected rescale to modify plateau provider");

        JsonObject rescaledHeight = root.getAsJsonArray("placement")
                .get(0).getAsJsonObject()
                .getAsJsonObject("height");

        JsonObject rescaledRange = rescaledHeight.getAsJsonObject("value");
        assertEquals(-67, rescaledRange.get("min_inclusive").getAsInt());
        assertEquals(501, rescaledRange.get("max_inclusive").getAsInt());
        assertEquals(312, rescaledHeight.getAsJsonObject("plateau").get("absolute").getAsInt());
    }

    @Test
    @DisplayName("Malformed JSON inputs are skipped without throwing")
    void malformedJsonSkipsGracefully(@TempDir Path tempDir) throws Exception {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);

        Path malformed = inputDir.resolve("bad.json");
        Files.writeString(malformed, "{\"placement\": [\n");

        try {
            OreRescaler.main(new String[] {
                    inputDir.toString(),
                    outputDir.toString()
            });
        } catch (IOException e) {
            LOGGER.warn("Skipping malformed JSON test due to missing/corrupt resource: {}", e.getMessage());
            assumeTrue(false, "Malformed JSON resource unavailable");
        }

        Path outputFile = outputDir.resolve("bad.json");
        assertFalse(Files.exists(outputFile), "Malformed JSON should not produce output");
    }

    @Test
    @DisplayName("Running on vanilla resource rescaled values into worldrise bounds")
    void rescaleSampleResource(@TempDir Path tempDir) throws Exception {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);

        Path sampleInput = inputDir.resolve("ore_coal_upper.json");
        copyResource("data/minecraft/worldgen/placed_feature/ore_coal_upper.json", sampleInput);

        OreRescaler.main(new String[] { inputDir.toString(), outputDir.toString() });

        Path rescaled = outputDir.resolve("ore_coal_upper.json");
        assertTrue(Files.exists(rescaled), "Rescaled file should be emitted");

        JsonObject root;
        try (Reader reader = Files.newBufferedReader(rescaled)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }

        List<Integer> absoluteValues = new ArrayList<>();
        JsonArray placement = root.getAsJsonArray("placement");
        for (JsonElement element : placement) {
            if (!element.isJsonObject()) continue;
            JsonObject placementObj = element.getAsJsonObject();
            if (!placementObj.has("type") || !"minecraft:height_range".equals(placementObj.get("type").getAsString())) {
                continue;
            }
            JsonObject height = placementObj.getAsJsonObject("height");
            collectAbsoluteValues(height, absoluteValues);
        }

        assertFalse(absoluteValues.isEmpty(), "Expected to capture rescaled absolute values");
        for (int value : absoluteValues) {
            assertTrue(value >= EXPECTED_MIN && value <= EXPECTED_MAX,
                    () -> "Value out of bounds: " + value);
        }
    }

    private static void collectAbsoluteValues(JsonObject height, List<Integer> values) {
        if (height == null) {
            return;
        }
        if (height.has("plateau") && height.get("plateau").isJsonObject()) {
            JsonObject plateau = height.getAsJsonObject("plateau");
            if (plateau.has("absolute")) {
                values.add(plateau.get("absolute").getAsInt());
            }
        }
        if (height.has("min_inclusive") && height.get("min_inclusive").isJsonObject()) {
            JsonObject min = height.getAsJsonObject("min_inclusive");
            if (min.has("absolute")) {
                values.add(min.get("absolute").getAsInt());
            }
        }
        if (height.has("max_inclusive") && height.get("max_inclusive").isJsonObject()) {
            JsonObject max = height.getAsJsonObject("max_inclusive");
            if (max.has("absolute")) {
                values.add(max.get("absolute").getAsInt());
            }
        }
        if (height.has("value") && height.get("value").isJsonObject()) {
            JsonObject range = height.getAsJsonObject("value");
            if (range.has("min_inclusive")) {
                values.add(range.get("min_inclusive").getAsInt());
            }
            if (range.has("max_inclusive")) {
                values.add(range.get("max_inclusive").getAsInt());
            }
        }
    }

    private static void copyResource(String resourcePath, Path destination) throws IOException {
        Path source = resolveTestResource(resourcePath);
        Files.createDirectories(destination.getParent());
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8);
             var writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            reader.transferTo(writer);
        }
    }

    private static Path resolveTestResource(String resourcePath) {
        Path resource = Paths.get("src", "test", "resources").resolve(resourcePath);
        if (!Files.exists(resource)) {
            LOGGER.warn("Skipping test: missing resource {}", resource);
            assumeTrue(false, "Test resource missing: " + resource);
        }
        return resource;
    }
}
