package com.yourorg.worldrise.data;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yourorg.worldrise.config.WorldriseConfig;
import com.yourorg.worldrise.data.BiomeModifierLoader.ScaledModifier;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BiomeModifierLoaderTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");

    private double defaultOre;
    private double defaultCarver;
    private List<String> oreOverrides;
    private List<String> carverOverrides;

    @BeforeEach
    void snapshotConfig() {
        WorldriseConfig config = WorldriseConfig.INSTANCE;
        defaultOre = config.defaultOreMultiplier.get();
        defaultCarver = config.defaultCarverMultiplier.get();
        oreOverrides = List.copyOf(config.biomeOreMultipliers.get());
        carverOverrides = List.copyOf(config.biomeCarverMultipliers.get());
    }

    @AfterEach
    void restoreConfig() {
        WorldriseConfig config = WorldriseConfig.INSTANCE;
        config.defaultOreMultiplier.set(defaultOre);
        config.defaultCarverMultiplier.set(defaultCarver);
        config.biomeOreMultipliers.set(oreOverrides);
        config.biomeCarverMultipliers.set(carverOverrides);
    }

    @Test
    @DisplayName("Ore overrides apply to placed feature placement counts")
    void oreOverridesApplyToPlacedFeatures() throws Exception {
        WorldriseConfig.INSTANCE.defaultOreMultiplier.set(2.0);
        WorldriseConfig.INSTANCE.biomeOreMultipliers
                .set(List.of("#worldrise:ores_overworld_biomes=0.5"));

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(RESOURCE_ROOT, "add_scaled_ores");
        assertEquals(7, scaled.placedFeatures().size(),
                "Expected all scaled ore placed features to be loaded");

        JsonObject coalFeature = scaled.placedFeatures().get("worldrise:ore_coal_scaled");
        assertNotNull(coalFeature, "Coal placed feature should be present");
        int count = extractPlacementValue(coalFeature, "minecraft:count", "count");
        assertEquals(10, count, "Override should scale coal count down from 20 to 10");
    }

    @Test
    @DisplayName("Default multiplier is used when no ore override matches")
    void defaultOreMultiplierUsedWhenNoOverrideMatches() throws Exception {
        WorldriseConfig.INSTANCE.defaultOreMultiplier.set(2.0);
        WorldriseConfig.INSTANCE.biomeOreMultipliers.set(List.of());

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(RESOURCE_ROOT, "add_scaled_ores");
        JsonObject coalFeature = scaled.placedFeatures().get("worldrise:ore_coal_scaled");
        int count = extractPlacementValue(coalFeature, "minecraft:count", "count");
        assertEquals(40, count, "Default multiplier should double the coal placement count");
    }

    @Test
    @DisplayName("Carver overrides adjust configured carver probability")
    void carverOverridesApplyProbabilityScaling() throws Exception {
        WorldriseConfig.INSTANCE.defaultCarverMultiplier.set(0.5);
        WorldriseConfig.INSTANCE.biomeCarverMultipliers
                .set(List.of("#worldrise:ocean_like=2.0"));

        ScaledModifier scaled = BiomeModifierLoader.loadScaledModifier(RESOURCE_ROOT, "add_ocean_canyon");
        JsonObject carver = scaled.configuredCarvers().get("worldrise:ocean_canyon");
        assertNotNull(carver, "Ocean canyon carver should be present");
        JsonObject config = carver.getAsJsonObject("config");
        double probability = config.get("probability").getAsDouble();
        assertEquals(0.04, probability, 1e-6,
                "Override should double the 0.02 probability while ignoring the 0.5 default");
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

