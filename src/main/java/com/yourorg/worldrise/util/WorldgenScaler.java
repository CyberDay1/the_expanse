package com.yourorg.worldrise.util;

import com.google.gson.JsonObject;
import com.yourorg.worldrise.config.WorldriseConfig;

public final class WorldgenScaler {

    private WorldgenScaler() {}

    public static void applyOreMultiplier(JsonObject placement) {
        double mult = WorldriseConfig.INSTANCE.oreDensityMultiplier.get();
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

    public static void applyCarverMultiplier(JsonObject config) {
        double mult = WorldriseConfig.INSTANCE.carverChanceMultiplier.get();
        if (config.has("probability")) {
            double prob = config.get("probability").getAsDouble();
            prob = Math.max(0.0, Math.min(1.0, prob * mult));
            config.addProperty("probability", prob);
        }
    }
}
