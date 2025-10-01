package com.theexpanse.data;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theexpanse.config.TheExpanseConfig;
import com.theexpanse.data.BiomeModifierLoader.ScaledModifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BiomeModifierLoaderTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");

    private double defaultOre;
    private double defaultCarver;
    private String oreOverrides;
    private String carverOverrides;
    private boolean ftbMaterialsCompat;
    private boolean silentGearCompat;
    private boolean productiveMetalworksCompat;

    @BeforeEach
    void snapshotConfig() {
        TheExpanseConfig config = TheExpanseConfig.INSTANCE;
        defaultOre = config.defaultOreMultiplier.get();
        defaultCarver = config.defaultCarverMultiplier.get();
        oreOverrides = config.biomeOreMultipliersRaw.get();
        carverOverrides = config.biomeCarverMultipliersRaw.get();
        ftbMaterialsCompat = config.ftbMaterialsCompatEnabled.get();
        silentGearCompat = config.silentGearCompatEnabled.get();
        productiveMetalworksCompat = config.productiveMetalworksCompatEnabled.get();
    }

    @AfterEach
    void restoreConfig() {
        TheExpanseConfig config = TheExpanseConfig.INSTANCE;
        config.defaultOreMultiplier.set(defaultOre);
        config.defaultCarverMultiplier.set(defaultCarver);
        config.biomeOreMultipliersRaw.set(oreOverrides);
        config.biomeCarverMultipliersRaw.set(carverOverrides);
        config.ftbMaterialsCompatEnabled.set(ftbMaterialsCompat);
        config.silentGearCompatEnabled.set(silentGearCompat);
        config.productiveMetalworksCompatEnabled.set(productiveMetalworksCompat);
    }

    @Test
    @DisplayName("Ore overrides apply to placed feature placement counts")
    void oreOverridesApplyToPlacedFeatures() throws Exception {
        TheExpanseConfig.INSTANCE.defaultOreMultiplier.set(2.0);
        TheExpanseConfig.INSTANCE.biomeOreMultipliersRaw
                .set("#the-expanse:ores_overworld_biomes=0.5");

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(RESOURCE_ROOT, "add_scaled_ores");
        assertEquals(7, scaled.placedFeatures().size(),
                "Expected all scaled ore placed features to be loaded");

        JsonObject coalFeature = scaled.placedFeatures().get("the-expanse:ore_coal_scaled");
        assertNotNull(coalFeature, "Coal placed feature should be present");
        int count = extractPlacementValue(coalFeature, "minecraft:count", "count");
        assertEquals(10, count, "Override should scale coal count down from 20 to 10");
    }

    @Test
    @DisplayName("Default multiplier is used when no ore override matches")
    void defaultOreMultiplierUsedWhenNoOverrideMatches() throws Exception {
        TheExpanseConfig.INSTANCE.defaultOreMultiplier.set(2.0);
        TheExpanseConfig.INSTANCE.biomeOreMultipliersRaw.set("");

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(RESOURCE_ROOT, "add_scaled_ores");
        JsonObject coalFeature = scaled.placedFeatures().get("the-expanse:ore_coal_scaled");
        int count = extractPlacementValue(coalFeature, "minecraft:count", "count");
        assertEquals(40, count, "Default multiplier should double the coal placement count");
    }

    @Test
    @DisplayName("Carver overrides adjust configured carver probability")
    void carverOverridesApplyProbabilityScaling() throws Exception {
        TheExpanseConfig.INSTANCE.defaultCarverMultiplier.set(0.5);
        TheExpanseConfig.INSTANCE.biomeCarverMultipliersRaw
                .set("#the-expanse:ocean_like=2.0");

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(RESOURCE_ROOT, "add_ocean_canyon");
        JsonObject carver = scaled.configuredCarvers().get("the-expanse:ocean_canyon");
        assertNotNull(carver, "Ocean canyon carver should be present");
        JsonObject config = carver.getAsJsonObject("config");
        double probability = config.get("probability").getAsDouble();
        assertEquals(0.04, probability, 1e-6,
                "Override should double the 0.02 probability while ignoring the 0.5 default");
    }

    @Test
    @DisplayName("Compat namespaces rescale height ranges when enabled")
    void compatNamespacesRescaleWhenEnabled(@TempDir Path tempDir) throws Exception {
        Path modifierDir = tempDir.resolve(Path.of("data", "the-expanse", "worldgen", "biome_modifier"));
        Files.createDirectories(modifierDir);
        Files.writeString(modifierDir.resolve("compat_test.json"),
                "{\n"
                        + "  \"type\": \"neoforge:add_features\",\n"
                        + "  \"biomes\": \"minecraft:plains\",\n"
                        + "  \"features\": [\"ftbmaterials:ore_test\"],\n"
                        + "  \"step\": \"underground_ores\"\n"
                        + "}\n",
                StandardCharsets.UTF_8);

        Path featureDir = tempDir.resolve(Path.of("data", "ftbmaterials", "worldgen", "placed_feature"));
        Files.createDirectories(featureDir);
        Files.writeString(featureDir.resolve("ore_test.json"),
                "{\n"
                        + "  \"feature\": \"ftbmaterials:ore_test_configured\",\n"
                        + "  \"placement\": [\n"
                        + "    {\n"
                        + "      \"type\": \"minecraft:height_range\",\n"
                        + "      \"height\": {\n"
                        + "        \"min_inclusive\": { \"absolute\": -64 },\n"
                        + "        \"max_inclusive\": { \"absolute\": 320 }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n",
                StandardCharsets.UTF_8);

        TheExpanseConfig.INSTANCE.ftbMaterialsCompatEnabled.set(true);
        TheExpanseConfig.INSTANCE.silentGearCompatEnabled.set(false);
        TheExpanseConfig.INSTANCE.productiveMetalworksCompatEnabled.set(false);

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(tempDir, "compat_test");
        JsonObject feature = scaled.placedFeatures().get("ftbmaterials:ore_test");
        assertNotNull(feature, "Compat feature should be loaded");

        JsonArray placements = feature.getAsJsonArray("placement");
        JsonObject placement = placements.get(0).getAsJsonObject();
        JsonObject height = placement.getAsJsonObject("height");
        JsonObject min = height.getAsJsonObject("min_inclusive");
        JsonObject max = height.getAsJsonObject("max_inclusive");
        assertEquals(-256, min.get("absolute").getAsInt(),
                "Minimum height should be rescaled to TheExpanse bounds");
        assertEquals(2015, max.get("absolute").getAsInt(),
                "Maximum height should be rescaled to TheExpanse bounds");
    }

    @Test
    @DisplayName("Compat namespaces honour disabled toggles")
    void compatNamespacesSkipWhenDisabled(@TempDir Path tempDir) throws Exception {
        Path modifierDir = tempDir.resolve(Path.of("data", "the-expanse", "worldgen", "biome_modifier"));
        Files.createDirectories(modifierDir);
        Files.writeString(modifierDir.resolve("compat_test.json"),
                "{\n"
                        + "  \"type\": \"neoforge:add_features\",\n"
                        + "  \"biomes\": \"minecraft:plains\",\n"
                        + "  \"features\": [\"silentgear:ore_test\"],\n"
                        + "  \"step\": \"underground_ores\"\n"
                        + "}\n",
                StandardCharsets.UTF_8);

        Path featureDir = tempDir.resolve(Path.of("data", "silentgear", "worldgen", "placed_feature"));
        Files.createDirectories(featureDir);
        Files.writeString(featureDir.resolve("ore_test.json"),
                "{\n"
                        + "  \"feature\": \"silentgear:ore_test_configured\",\n"
                        + "  \"placement\": [\n"
                        + "    {\n"
                        + "      \"type\": \"minecraft:height_range\",\n"
                        + "      \"height\": {\n"
                        + "        \"min_inclusive\": { \"absolute\": -64 },\n"
                        + "        \"max_inclusive\": { \"absolute\": 320 }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n",
                StandardCharsets.UTF_8);

        TheExpanseConfig.INSTANCE.ftbMaterialsCompatEnabled.set(false);
        TheExpanseConfig.INSTANCE.silentGearCompatEnabled.set(false);
        TheExpanseConfig.INSTANCE.productiveMetalworksCompatEnabled.set(false);

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(tempDir, "compat_test");
        JsonObject feature = scaled.placedFeatures().get("silentgear:ore_test");
        assertNotNull(feature, "Compat feature should be loaded");

        JsonArray placements = feature.getAsJsonArray("placement");
        JsonObject placement = placements.get(0).getAsJsonObject();
        JsonObject height = placement.getAsJsonObject("height");
        JsonObject min = height.getAsJsonObject("min_inclusive");
        JsonObject max = height.getAsJsonObject("max_inclusive");
        assertEquals(-64, min.get("absolute").getAsInt(),
                "Minimum height should remain unchanged when compat disabled");
        assertEquals(320, max.get("absolute").getAsInt(),
                "Maximum height should remain unchanged when compat disabled");
    }

    private static int extractPlacementValue(JsonObject placedFeature, String type, String property) {
        assertTrue(placedFeature.has("placement"), "Placed feature must declare placements");
        JsonArray placements = placedFeature.getAsJsonArray("placement");
        for (JsonElement element : placements) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject placement = element.getAsJsonObject();
            if (!placement.has("type") || !type.equals(placement.get("type").getAsString())) {
                continue;
            }
            assertTrue(placement.has(property),
                    () -> "Placement " + type + " is missing property " + property);
            return placement.get(property).getAsInt();
        }
        fail("Could not find placement of type " + type);
        return -1; // Unreachable
    }
}

