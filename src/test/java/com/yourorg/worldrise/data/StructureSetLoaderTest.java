package com.yourorg.worldrise.data;

import static org.junit.jupiter.api.Assertions.*;

import com.yourorg.worldrise.data.StructureSetLoader.StructureSetToggles;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StructureSetLoaderTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");

    @Test
    @DisplayName("All structure set overrides are enabled by default")
    void allStructureSetsEnabledByDefault() {
        List<Path> enabled = StructureSetLoader.enabledStructureSets(RESOURCE_ROOT,
                StructureSetToggles.of(true, true, true));

        assertEquals(3, enabled.size(), "Expected three structure set overrides to load");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("stronghold.json")),
                "Stronghold override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ancient_city.json")),
                "Ancient city override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("mineshaft.json")),
                "Mineshaft override should be included");
    }

    @Test
    @DisplayName("Disabled toggles skip the corresponding structure set overrides")
    void disabledTogglesSkipOverrides() {
        List<Path> enabled = StructureSetLoader.enabledStructureSets(RESOURCE_ROOT,
                StructureSetToggles.of(false, true, false));

        assertEquals(1, enabled.size(), "Only ancient city override should remain active");
        assertTrue(enabled.get(0).endsWith("ancient_city.json"),
                "Ancient city override should be the only active entry");
    }
}
