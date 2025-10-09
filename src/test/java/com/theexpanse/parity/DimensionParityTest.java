package com.theexpanse.parity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DimensionParityTest {
    private static final Path BASE_RESOURCES = ParityTestSupport.baseResources();
    private static final Path DIMENSION_RELATIVE = Path.of("data", "minecraft", "dimension");

    @Test
    void dimensionJsonsLoadAndMatchAcrossVariants() throws IOException {
        Map<String, JsonElement> baseline = ParityTestSupport.loadMergedJson(BASE_RESOURCES, null, DIMENSION_RELATIVE);
        Assumptions.assumeFalse(baseline.isEmpty(),
            "No dimension JSON files were discovered under " + DIMENSION_RELATIVE + ".");

        baseline.forEach((key, value) -> validateDimension(value, key));

        for (String variant : ParityTestSupport.listVariants()) {
            Path variantResources = ParityTestSupport.resolveVariantResources(variant);
            Map<String, JsonElement> candidate =
                ParityTestSupport.loadMergedJson(BASE_RESOURCES, variantResources, DIMENSION_RELATIVE);

            candidate.forEach((key, value) ->
                validateDimension(value, key + " (" + variant + ")"));
            assertEquals(baseline, candidate, () -> "Dimension JSON diverged for variant " + variant);
        }
    }

    private static void validateDimension(JsonElement element, String identifier) {
        assertTrue(element.isJsonObject(), () -> "Dimension definition is not an object for " + identifier);
        JsonObject dimension = element.getAsJsonObject();
        assertTrue(dimension.has("type"), () -> "Missing type for dimension " + identifier);
        assertTrue(dimension.get("type").isJsonPrimitive(),
            () -> "Dimension type must be a primitive for " + identifier);

        assertTrue(dimension.has("generator"), () -> "Missing generator block for " + identifier);
        JsonObject generator = dimension.getAsJsonObject("generator");
        assertTrue(generator.has("type"), () -> "Missing generator.type for " + identifier);
        assertTrue(generator.get("type").isJsonPrimitive(),
            () -> "generator.type must be a primitive for " + identifier);

        assertTrue(generator.has("biome_source"), () -> "Missing biome_source in generator for " + identifier);
        JsonObject biomeSource = generator.getAsJsonObject("biome_source");
        assertTrue(biomeSource.has("type"), () -> "Missing biome_source.type for " + identifier);
        assertTrue(biomeSource.get("type").isJsonPrimitive(),
            () -> "biome_source.type must be a primitive for " + identifier);
    }
}
