package com.yourorg.worldrise.util;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public final class OreRescaler {
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
                    boolean changed = HeightRescaler.rescalePlacedFeature(root);
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

}

