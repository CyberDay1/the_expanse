package com.yourorg.worldrise.data;

import static org.junit.jupiter.api.Assertions.*;

import com.yourorg.worldrise.data.CarverLoader.CarverToggles;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CarverLoaderTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");

    @Test
    @DisplayName("All carver resources are enabled by default")
    void allCarversEnabledByDefault() {
        List<Path> enabled = CarverLoader.enabledCarvers(RESOURCE_ROOT,
                CarverToggles.of(true, true, true, true));

        assertEquals(8, enabled.size(),
                "Expected four carvers and four biome modifiers to load when enabled");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ocean_canyon.json")),
                "Ocean canyon carver should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("add_ocean_canyon.json")),
                "Ocean canyon biome modifier should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("mega_ravine.json")),
                "Mega ravine carver should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("add_mega_ravines.json")),
                "Mega ravine biome modifier should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("sinkhole.json")),
                "Sinkhole carver should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("add_sinkholes.json")),
                "Sinkhole biome modifier should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ocean_blue_hole.json")),
                "Ocean blue hole carver should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("add_blue_holes.json")),
                "Ocean blue hole biome modifier should be included");
    }

    @Test
    @DisplayName("Disabled toggles remove carver resources")
    void disabledTogglesRemoveCarvers() {
        List<Path> enabled = CarverLoader.enabledCarvers(RESOURCE_ROOT,
                CarverToggles.of(false, true, false, false));

        assertEquals(2, enabled.size(),
                "Only mega ravine carver and modifier should remain active");
        assertTrue(enabled.stream().allMatch(path ->
                path.endsWith("mega_ravine.json") || path.endsWith("add_mega_ravines.json")),
                "Only mega ravine resources should be returned");
    }
}
