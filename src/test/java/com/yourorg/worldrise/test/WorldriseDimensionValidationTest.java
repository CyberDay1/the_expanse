package com.yourorg.worldrise.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class WorldriseDimensionValidationTest {

    private JsonObject loadJson(String resourcePath) throws IOException {
        Path path = Paths.get("src/main/resources").resolve(resourcePath);
        assertTrue(Files.exists(path), "Missing resource: " + path);
        return JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
    }

    @Test
    public void testOverworldDimensionType() throws Exception {
        JsonObject json = loadJson("data/minecraft/dimension_type/overworld.json");
        int minY = json.get("min_y").getAsInt();
        int height = json.get("height").getAsInt();
        int logicalHeight = json.get("logical_height").getAsInt();
        int seaLevel = json.get("sea_level").getAsInt();

        try {
            assertEquals(-256, minY, "Overworld min_y must be -256");
            assertEquals(2272, height, "Overworld height must be 2272");
            assertEquals(2272, logicalHeight, "Overworld logical_height must be 2272");
            assertEquals(150, seaLevel, "Overworld sea_level must be 150");
        } catch (AssertionError e) {
            System.err.println("Dimension JSON mismatch:");
            System.err.printf("min_y=%d, height=%d, logical_height=%d, sea_level=%d%n",
                    minY, height, logicalHeight, seaLevel);
            throw e;
        }
    }

    @Test
    public void testOceanCanyonCarver() throws Exception {
        JsonObject json = loadJson("data/worldrise/worldgen/configured_carver/ocean_canyon.json")
                .getAsJsonObject("config");
        double hrm = json.get("horizontal_radius_multiplier").getAsDouble();
        assertEquals(1.5, hrm, 1e-6, "Ocean canyon horizontal_radius_multiplier must be 1.5");
    }
}
