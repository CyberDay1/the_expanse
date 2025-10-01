package com.yourorg.worldrise.util;

import com.google.gson.JsonObject;
import com.yourorg.worldrise.config.WorldriseConfig;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WorldgenScaler {

    private WorldgenScaler() {}

    public static void applyOreMultiplier(JsonObject placement) {
        applyOreMultiplier(placement, Collections.emptyList());
    }

    public static void applyOreMultiplier(JsonObject placement, Collection<String> biomeSelectors) {
        double mult = resolveOreMultiplier(biomeSelectors);
        applyOreMultiplier(placement, mult);
    }

    public static void applyCarverMultiplier(JsonObject config) {
        applyCarverMultiplier(config, Collections.emptyList());
    }

    public static void applyCarverMultiplier(JsonObject config, Collection<String> biomeSelectors) {
        double mult = resolveCarverMultiplier(biomeSelectors);
        applyCarverMultiplier(config, mult);
    }

    private static void applyOreMultiplier(JsonObject placement, double mult) {
        if (placement.has("count")) {
            int count = placement.get("count").getAsInt();
            int newCount = Math.max(1, (int) Math.round(count * mult));
            placement.addProperty("count", newCount);
        }
        if (placement.has("tries")) {
            int tries = placement.get("tries").getAsInt();
            int newTries = Math.max(1, (int) Math.round(tries * mult));
            placement.addProperty("tries", newTries);
        }
    }

    private static void applyCarverMultiplier(JsonObject config, double mult) {
        if (config.has("probability")) {
            double prob = config.get("probability").getAsDouble();
            prob = Math.max(0.0, Math.min(1.0, prob * mult));
            config.addProperty("probability", prob);
        }
    }

    private static double resolveOreMultiplier(Collection<String> biomeSelectors) {
        return resolveMultiplier(biomeSelectors, WorldriseConfig.INSTANCE.defaultOreMultiplier.get(),
                WorldriseConfig.INSTANCE.getBiomeOreMultiplierEntries());
    }

    private static double resolveCarverMultiplier(Collection<String> biomeSelectors) {
        return resolveMultiplier(biomeSelectors, WorldriseConfig.INSTANCE.defaultCarverMultiplier.get(),
                WorldriseConfig.INSTANCE.getBiomeCarverMultiplierEntries());
    }

    private static double resolveMultiplier(Collection<String> biomeSelectors, double defaultMultiplier,
            List<String> overrides) {
        Map<String, Double> overrideMap = parseOverrides(overrides);
        if (biomeSelectors != null) {
            for (String selector : biomeSelectors) {
                if (selector == null) {
                    continue;
                }
                String normalized = selector.trim();
                if (normalized.isEmpty()) {
                    continue;
                }
                Double multiplier = overrideMap.get(normalized);
                if (multiplier != null) {
                    return multiplier;
                }
            }
        }
        return defaultMultiplier;
    }

    private static Map<String, Double> parseOverrides(List<? extends String> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> parsed = new LinkedHashMap<>();
        for (String entry : overrides) {
            if (entry == null) {
                continue;
            }
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int equals = trimmed.indexOf('=');
            if (equals <= 0 || equals == trimmed.length() - 1) {
                continue;
            }
            String selector = trimmed.substring(0, equals).trim();
            String valuePart = trimmed.substring(equals + 1).trim();
            if (selector.isEmpty() || valuePart.isEmpty()) {
                continue;
            }
            try {
                double value = Double.parseDouble(valuePart);
                if (value >= 0.0) {
                    parsed.put(selector, value);
                }
            } catch (NumberFormatException ignored) {
                // Skip malformed entries.
            }
        }
        return parsed;
    }

    public static Set<String> collectCompatNamespaces(WorldriseConfig config) {
        Set<String> namespaces = new LinkedHashSet<>();
        if (config.ftbMaterialsCompatEnabled.get()) {
            namespaces.add("ftbmaterials");
        }
        if (config.silentGearCompatEnabled.get()) {
            namespaces.add("silentgear");
        }
        if (config.productiveMetalworksCompatEnabled.get()) {
            namespaces.add("productive_metalworks");
        }
        return namespaces;
    }
}
