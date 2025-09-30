package com.yourorg.worldrise.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class WorldriseDimensionValidationTest {

    private JsonObject loadJson(String path) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, "Missing resource: " + path);
            return JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        }
    }

    @Test
    public void testOverworldDimensionType() throws Exception {
        JsonObject json = loadJson("data/worldrise/dimension_type/overworld.json");
        assertEquals(-256, json.get("min_y").getAsInt(), "Overworld min_y must be -256");
        assertEquals(2272, json.get("height").getAsInt(), "Overworld height must be 2272");
        assertEquals(2272, json.get("logical_height").getAsInt(), "Overworld logical_height must be 2272");
    }

    @Test
    public void testOceanCanyonCarver() throws Exception {
        JsonObject json = loadJson("data/worldrise/worldgen/configured_carver/ocean_canyon.json")
                .getAsJsonObject("config");
        double hrm = json.get("horizontal_radius_multiplier").getAsDouble();
        assertEquals(1.5, hrm, 1e-6, "Ocean canyon horizontal_radius_multiplier must be 1.5");
    }
}
