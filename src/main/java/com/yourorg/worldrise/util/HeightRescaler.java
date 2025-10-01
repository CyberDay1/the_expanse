package com.yourorg.worldrise.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;

/**
 * Utility responsible for expanding vanilla ore placement height providers so they
 * fit within Worldrise's extended build range. The logic mirrors the standalone
 * {@link OreRescaler} tool so runtime compatibility paths can reuse the same
 * behaviour when mutating loaded JSON definitions.
 */
public final class HeightRescaler {

    private static final int VANILLA_MIN = -64;
    private static final int VANILLA_MAX = 320;
    private static final int WORLD_MIN = -256;
    private static final int WORLD_MAX = 2015;
    private static final double SCALE = (double) (WORLD_MAX - WORLD_MIN)
            / (VANILLA_MAX - VANILLA_MIN);

    private HeightRescaler() { }

    /**
     * Attempts to rescale every height provider referenced by the supplied placed
     * feature definition. The method mirrors the behaviour of the CLI rescale tool
     * and therefore only mutates {@code minecraft:height_range} placement entries.
     *
     * @param placedFeature JSON definition of the placed feature to mutate
     * @return {@code true} if any height values were adjusted, {@code false}
     *         otherwise
     */
    public static boolean rescalePlacedFeature(JsonObject placedFeature) {
        if (placedFeature == null || !placedFeature.has("placement")
                || !placedFeature.get("placement").isJsonArray()) {
            return false;
        }

        boolean changed = false;
        for (JsonElement element : placedFeature.getAsJsonArray("placement")) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject placement = element.getAsJsonObject();
            if (!placement.has("type")
                    || !"minecraft:height_range".equals(placement.get("type").getAsString())) {
                continue;
            }
            if (!placement.has("height") || !placement.get("height").isJsonObject()) {
                continue;
            }
            changed |= rescaleHeightProvider(placement.getAsJsonObject("height"));
        }
        return changed;
    }

    /**
     * Rescales a single height provider JSON object in-place.
     */
    private static boolean rescaleHeightProvider(JsonObject provider) {
        boolean changed = false;
        for (Map.Entry<String, JsonElement> entry : provider.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (("min_inclusive".equals(key) || "max_inclusive".equals(key)
                    || "plateau".equals(key)) && value.isJsonObject()) {
                JsonObject child = value.getAsJsonObject();
                if (child.has("absolute")) {
                    int oldY = child.get("absolute").getAsInt();
                    child.addProperty("absolute", rescaleY(oldY));
                    changed = true;
                }
            } else if ("value".equals(key) && value.isJsonObject()) {
                JsonObject range = value.getAsJsonObject();
                if (range.has("min_inclusive") && range.has("max_inclusive")) {
                    int min = range.get("min_inclusive").getAsInt();
                    int max = range.get("max_inclusive").getAsInt();
                    range.addProperty("min_inclusive", rescaleY(min));
                    range.addProperty("max_inclusive", rescaleY(max));
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * Converts a vanilla-height Y coordinate into the expanded Worldrise range.
     */
    public static int rescaleY(int y) {
        return (int) Math.round(WORLD_MIN + (y - VANILLA_MIN) * SCALE);
    }
}

