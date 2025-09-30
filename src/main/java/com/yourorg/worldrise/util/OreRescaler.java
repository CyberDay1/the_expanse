package com.yourorg.worldrise.util;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.Map;

public final class OreRescaler {

    private static final int VANILLA_MIN = -64;
    private static final int VANILLA_MAX = 320;
    private static final int WORLD_MIN   = -256;
    private static final int WORLD_MAX   = 2015;
    private static final double SCALE = (double)(WORLD_MAX - WORLD_MIN) / (VANILLA_MAX - VANILLA_MIN);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: OreRescaler <inputDir> <outputDir>");
            System.exit(1);
        }
        Path inputDir = Paths.get(args[0]);
        Path outputDir = Paths.get(args[1]);

        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input dir does not exist: " + inputDir);
        }
        Files.createDirectories(outputDir);

        try (var stream = Files.walk(inputDir)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try {
                    JsonObject root = JsonParser.parseReader(new FileReader(p.toFile())).getAsJsonObject();
                    boolean changed = rescaleHeight(root);
                    if (changed) {
                        Path rel = inputDir.relativize(p);
                        Path outFile = outputDir.resolve(rel);
                        Files.createDirectories(outFile.getParent());
                        try (Writer w = new FileWriter(outFile.toFile())) {
                            GSON.toJson(root, w);
                        }
                        System.out.println("Rescaled: " + rel);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing " + p + ": " + e.getMessage());
                }
            });
        }
    }

    private static boolean rescaleHeight(JsonObject root) {
        if (!root.has("placement")) return false;
        boolean changed = false;
        for (JsonElement elem : root.getAsJsonArray("placement")) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("type") && "minecraft:height_range".equals(obj.get("type").getAsString())) {
                    if (obj.has("height") && obj.get("height").isJsonObject()) {
                        JsonObject height = obj.getAsJsonObject("height");
                        changed |= rescaleHeightProvider(height);
                    }
                }
            }
        }
        return changed;
    }

    private static boolean rescaleHeightProvider(JsonObject provider) {
        boolean changed = false;
        for (Map.Entry<String, JsonElement> e : provider.entrySet()) {
            String key = e.getKey();
            JsonElement value = e.getValue();
            if ((key.equals("min_inclusive") || key.equals("max_inclusive") || key.equals("plateau")) && value.isJsonObject()) {
                JsonObject valObj = value.getAsJsonObject();
                if (valObj.has("absolute")) {
                    int oldY = valObj.get("absolute").getAsInt();
                    int newY = rescaleY(oldY);
                    valObj.addProperty("absolute", newY);
                    changed = true;
                }
            } else if (key.equals("value") && value.isJsonObject()) {
                JsonObject valObj = value.getAsJsonObject();
                if (valObj.has("min_inclusive") && valObj.has("max_inclusive")) {
                    int oldMin = valObj.get("min_inclusive").getAsInt();
                    int oldMax = valObj.get("max_inclusive").getAsInt();
                    valObj.addProperty("min_inclusive", rescaleY(oldMin));
                    valObj.addProperty("max_inclusive", rescaleY(oldMax));
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static int rescaleY(int y) {
        return (int)Math.round(WORLD_MIN + (y - VANILLA_MIN) * SCALE);
    }
}

