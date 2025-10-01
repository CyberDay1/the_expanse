package com.theexpanse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

class TheExpanseDimensionSanityTest {
    private static final Gson GSON = new Gson();

    private JsonObject loadResource(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(in, "Missing resource: " + path);
        return GSON.fromJson(new InputStreamReader(in), JsonObject.class);
    }

    @Test
    void testOverworldBounds() {
        JsonObject obj = loadResource("data/minecraft/dimension_type/overworld.json");
        assertEquals(-256, obj.get("min_y").getAsInt());
        assertEquals(2272, obj.get("height").getAsInt());
        assertEquals(2272, obj.get("logical_height").getAsInt());
        assertEquals(150, obj.get("sea_level").getAsInt());
    }

    @Test
    void testNetherBounds() {
        JsonObject obj = loadResource("data/minecraft/dimension_type/the_nether.json");
        assertEquals(-256, obj.get("min_y").getAsInt());
        assertEquals(2272, obj.get("height").getAsInt());
        assertEquals(2272, obj.get("logical_height").getAsInt());
        assertEquals(32, obj.get("sea_level").getAsInt());
    }

    @Test
    void testEndBounds() {
        JsonObject obj = loadResource("data/minecraft/dimension_type/the_end.json");
        assertEquals(-256, obj.get("min_y").getAsInt());
        assertEquals(2272, obj.get("height").getAsInt());
        assertEquals(2272, obj.get("logical_height").getAsInt());
        assertEquals(0, obj.get("sea_level").getAsInt());
    }
}
