package com.theexpanse.data;

import static org.junit.jupiter.api.Assertions.*;

import com.theexpanse.data.DimensionLoader.DimensionToggles;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DimensionLoaderTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");

    @Test
    @DisplayName("All dimension overrides are enabled by default")
    void allDimensionsEnabledByDefault() {
        List<Path> enabled = DimensionLoader.enabledDimensions(RESOURCE_ROOT,
                DimensionToggles.of(true, true));

        assertEquals(4, enabled.size(), "Expected both nether and end overrides to load");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("dimension_type/nether.json")),
                "Nether dimension type override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("dimension/nether.json")),
                "Vanilla nether override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("dimension_type/end.json")),
                "End dimension type override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("dimension/end.json")),
                "Vanilla end override should be included");
    }

    @Test
    @DisplayName("Disabled toggles skip the corresponding dimension overrides")
    void disabledTogglesSkipOverrides() {
        List<Path> enabled = DimensionLoader.enabledDimensions(RESOURCE_ROOT,
                DimensionToggles.of(true, false));

        assertEquals(2, enabled.size(), "Only nether overrides should remain active");
        assertTrue(enabled.stream().allMatch(path -> path.toString().contains("nether")),
                "Only nether override files should be enabled");
    }
}
