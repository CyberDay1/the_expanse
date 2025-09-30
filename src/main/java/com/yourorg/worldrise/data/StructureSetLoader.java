package com.yourorg.worldrise.data;

import com.yourorg.worldrise.config.WorldriseConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility responsible for determining which structure set overrides should be applied. The
 * selection is controlled through configuration toggles so users can opt out of conflicting
 * datapack adjustments.
 */
public final class StructureSetLoader {

    private static final Path STRUCTURE_SET_ROOT =
            Path.of("data", "worldrise", "worldgen", "structure_set");

    private StructureSetLoader() { }

    /**
     * Resolves the structure set override files that should be loaded for the current
     * configuration.
     *
     * @param resourceRoot root folder that contains the mod data pack resources
     * @return immutable list of absolute paths to structure set overrides that should be applied
     */
    public static List<Path> enabledStructureSets(Path resourceRoot) {
        return enabledStructureSets(resourceRoot,
                StructureSetToggles.fromConfig(WorldriseConfig.INSTANCE));
    }

    static List<Path> enabledStructureSets(Path resourceRoot, StructureSetToggles toggles) {
        Path base = resourceRoot.resolve(STRUCTURE_SET_ROOT);
        List<Path> enabled = new ArrayList<>();
        addIfEnabled(enabled, base.resolve("stronghold.json"), toggles.strongholdScaling());
        addIfEnabled(enabled, base.resolve("ancient_city.json"), toggles.ancientCityScaling());
        addIfEnabled(enabled, base.resolve("mineshaft.json"), toggles.mineshaftScaling());
        addIfEnabled(enabled, base.resolve("nether_fortress.json"), toggles.fortressScaling());
        addIfEnabled(enabled, base.resolve("bastion.json"), toggles.bastionScaling());
        addIfEnabled(enabled, base.resolve("ocean_monument.json"), toggles.monumentScaling());
        addIfEnabled(enabled, base.resolve("end_city.json"), toggles.endCityScaling());
        return List.copyOf(enabled);
    }

    private static void addIfEnabled(List<Path> enabled, Path candidate, boolean isEnabled) {
        if (isEnabled && Files.exists(candidate)) {
            enabled.add(candidate);
        }
    }

    public interface StructureSetToggles {
        boolean strongholdScaling();

        boolean ancientCityScaling();

        boolean mineshaftScaling();

        boolean fortressScaling();

        boolean bastionScaling();

        boolean monumentScaling();

        boolean endCityScaling();

        static StructureSetToggles fromConfig(WorldriseConfig config) {
            return new StructureSetToggles() {
                @Override
                public boolean strongholdScaling() {
                    return config.strongholdScaling.get();
                }

                @Override
                public boolean ancientCityScaling() {
                    return config.ancientCityScaling.get();
                }

                @Override
                public boolean mineshaftScaling() {
                    return config.mineshaftScaling.get();
                }

                @Override
                public boolean fortressScaling() {
                    return config.fortressScaling.get();
                }

                @Override
                public boolean bastionScaling() {
                    return config.bastionScaling.get();
                }

                @Override
                public boolean monumentScaling() {
                    return config.monumentScaling.get();
                }

                @Override
                public boolean endCityScaling() {
                    return config.endCityScaling.get();
                }
            };
        }

        static StructureSetToggles of(boolean stronghold, boolean ancientCity, boolean mineshaft,
                boolean fortress, boolean bastion, boolean monument, boolean endCity) {
            return new StructureSetToggles() {
                @Override
                public boolean strongholdScaling() {
                    return stronghold;
                }

                @Override
                public boolean ancientCityScaling() {
                    return ancientCity;
                }

                @Override
                public boolean mineshaftScaling() {
                    return mineshaft;
                }

                @Override
                public boolean fortressScaling() {
                    return fortress;
                }

                @Override
                public boolean bastionScaling() {
                    return bastion;
                }

                @Override
                public boolean monumentScaling() {
                    return monument;
                }

                @Override
                public boolean endCityScaling() {
                    return endCity;
                }
            };
        }
    }
}

