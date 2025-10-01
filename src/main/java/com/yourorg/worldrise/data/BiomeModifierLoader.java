package com.yourorg.worldrise.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yourorg.worldrise.util.WorldgenScaler;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility that loads biome modifier JSON definitions and applies the configured scaling overrides to the
 * referenced placed features or configured carvers. The returned {@link ScaledModifier} exposes the mutated
 * JSON objects so callers can register them with the appropriate multipliers baked in.
 */
public final class BiomeModifierLoader {

    private static final Path BIOME_MODIFIER_ROOT = Path.of("data", "worldrise", "worldgen", "biome_modifier");
    private static final Path PLACED_FEATURE_DIR = Path.of("worldgen", "placed_feature");
    private static final Path CONFIGURED_CARVER_DIR = Path.of("worldgen", "configured_carver");

    private BiomeModifierLoader() { }

    /**
     * Loads the specified biome modifier JSON definition, applying ore/carver scaling overrides to the referenced
     * resources. All returned JSON objects are mutable copies so that downstream registration can further customise
     * them if required.
     *
     * @param resourceRoot root folder that contains the mod data pack resources
     * @param modifierName file name (without extension) of the biome modifier located in the worldrise namespace
     * @return wrapper containing the biome modifier JSON along with the scaled placed features and carvers it references
     * @throws IOException if any of the referenced resources cannot be read
     */
    public static ScaledModifier loadScaledModifier(Path resourceRoot, String modifierName) throws IOException {
        Path modifierPath = resourceRoot.resolve(BIOME_MODIFIER_ROOT).resolve(modifierName + ".json");
        JsonObject modifier = readJson(modifierPath);
        List<String> selectors = extractBiomeSelectors(modifier);

        Map<String, JsonObject> features = new LinkedHashMap<>();
        if (modifier.has("features")) {
            JsonArray featureIds = modifier.getAsJsonArray("features");
            for (JsonElement element : featureIds) {
                if (!element.isJsonPrimitive()) {
                    continue;
                }
                String featureId = element.getAsString();
                JsonObject featureJson = loadPlacedFeature(resourceRoot, featureId);
                scalePlacedFeature(featureJson, selectors);
                features.put(featureId, featureJson);
            }
        }

        Map<String, JsonObject> carvers = new LinkedHashMap<>();
        if (modifier.has("carvers")) {
            JsonArray carverIds = modifier.getAsJsonArray("carvers");
            for (JsonElement element : carverIds) {
                if (!element.isJsonPrimitive()) {
                    continue;
                }
                String carverId = element.getAsString();
                JsonObject carverJson = loadConfiguredCarver(resourceRoot, carverId);
                if (carverJson.has("config") && carverJson.get("config").isJsonObject()) {
                    WorldgenScaler.applyCarverMultiplier(carverJson.getAsJsonObject("config"), selectors);
                }
                carvers.put(carverId, carverJson);
            }
        }

        return new ScaledModifier(modifier.deepCopy(), Map.copyOf(features), Map.copyOf(carvers));
    }

    private static void scalePlacedFeature(JsonObject placedFeature, Collection<String> selectors) {
        if (!placedFeature.has("placement") || !placedFeature.get("placement").isJsonArray()) {
            return;
        }
        JsonArray placements = placedFeature.getAsJsonArray("placement");
        for (JsonElement placement : placements) {
            if (placement.isJsonObject()) {
                WorldgenScaler.applyOreMultiplier(placement.getAsJsonObject(), selectors);
            }
        }
    }

    private static JsonObject loadPlacedFeature(Path resourceRoot, String featureId) throws IOException {
        Path path = resolveWorldgenResource(resourceRoot, featureId, PLACED_FEATURE_DIR);
        return readJson(path);
    }

    private static JsonObject loadConfiguredCarver(Path resourceRoot, String carverId) throws IOException {
        Path path = resolveWorldgenResource(resourceRoot, carverId, CONFIGURED_CARVER_DIR);
        return readJson(path);
    }

    private static Path resolveWorldgenResource(Path resourceRoot, String location, Path subDirectory) throws IOException {
        String namespace;
        String path;
        int colon = location.indexOf(':');
        if (colon >= 0) {
            namespace = location.substring(0, colon);
            path = location.substring(colon + 1);
        } else {
            namespace = "minecraft";
            path = location;
        }

        Path resolved = resourceRoot.resolve("data").resolve(namespace);
        for (Path segment : subDirectory) {
            resolved = resolved.resolve(segment.toString());
        }

        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            resolved = resolved.resolve(parts[i]);
        }
        String fileName = parts[parts.length - 1] + ".json";
        resolved = resolved.resolve(fileName);
        if (!Files.exists(resolved)) {
            throw new IOException("Missing worldgen resource '" + location + "' at " + resolved);
        }
        return resolved;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static List<String> extractBiomeSelectors(JsonObject modifier) {
        if (!modifier.has("biomes")) {
            return Collections.emptyList();
        }
        JsonElement biomes = modifier.get("biomes");
        if (biomes.isJsonPrimitive()) {
            return List.of(biomes.getAsString());
        }
        if (biomes.isJsonArray()) {
            List<String> selectors = new ArrayList<>();
            for (JsonElement element : biomes.getAsJsonArray()) {
                if (element.isJsonPrimitive()) {
                    selectors.add(element.getAsString());
                }
            }
            return List.copyOf(selectors);
        }
        return Collections.emptyList();
    }

    /**
     * Container for a biome modifier JSON definition and the scaled resources it references.
     *
     * @param biomeModifier    biome modifier JSON document
     * @param placedFeatures   map of placed feature resource locations to their scaled JSON definitions
     * @param configuredCarvers map of configured carver resource locations to their scaled JSON definitions
     */
    public record ScaledModifier(JsonObject biomeModifier, Map<String, JsonObject> placedFeatures,
            Map<String, JsonObject> configuredCarvers) { }
}

