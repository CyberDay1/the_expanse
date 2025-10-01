package com.theexpanse.data;

import static org.junit.jupiter.api.Assertions.*;

import com.theexpanse.data.StructureSetLoader.StructureSetToggles;
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
                StructureSetToggles.of(true, true, true, true, true, true, true));

        assertEquals(7, enabled.size(), "Expected seven structure set overrides to load");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("stronghold.json")),
                "Stronghold override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ancient_city.json")),
                "Ancient city override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("mineshaft.json")),
                "Mineshaft override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("nether_fortress.json")),
                "Nether fortress override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("bastion.json")),
                "Bastion remnant override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ocean_monument.json")),
                "Ocean monument override should be included");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("end_city.json")),
                "End city override should be included");
    }

    @Test
    @DisplayName("Disabled toggles skip the corresponding structure set overrides")
    void disabledTogglesSkipOverrides() {
        List<Path> enabled = StructureSetLoader.enabledStructureSets(RESOURCE_ROOT,
                StructureSetToggles.of(false, true, false, false, false, true, false));

        assertEquals(2, enabled.size(),
                "Ancient city and ocean monument overrides should remain active");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ancient_city.json")),
                "Ancient city override should remain active");
        assertTrue(enabled.stream().anyMatch(path -> path.endsWith("ocean_monument.json")),
                "Ocean monument override should remain active");
    }
}
