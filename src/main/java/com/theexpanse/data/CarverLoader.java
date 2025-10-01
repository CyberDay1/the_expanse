package com.theexpanse.data;

import com.theexpanse.config.TheExpanseConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Determines which configured carvers and associated biome modifiers should be exposed by the
 * builtin datapack. Each toggle ensures that disabling a feature removes both the configured
 * carver and its biome modifier to prevent broken references during worldgen loading.
 */
public final class CarverLoader {

    private static final Path CARVER_ROOT =
            Path.of("data", "the-expanse", "worldgen", "configured_carver");
    private static final Path BIOME_MODIFIER_ROOT =
            Path.of("data", "the-expanse", "worldgen", "biome_modifier");

    private CarverLoader() { }

    /**
     * Resolves the carver related JSON resources that should be available given the active
     * configuration.
     *
     * @param resourceRoot root folder that contains the mod data pack resources
     * @return immutable list of JSON files representing the enabled carvers and modifiers
     */
    public static List<Path> enabledCarvers(Path resourceRoot) {
        return enabledCarvers(resourceRoot, CarverToggles.fromConfig(TheExpanseConfig.INSTANCE));
    }

    static List<Path> enabledCarvers(Path resourceRoot, CarverToggles toggles) {
        Path carverBase = resourceRoot.resolve(CARVER_ROOT);
        Path modifierBase = resourceRoot.resolve(BIOME_MODIFIER_ROOT);

        List<Path> enabled = new ArrayList<>();
        addIfEnabled(enabled, carverBase.resolve("ocean_canyon.json"),
                modifierBase.resolve("add_ocean_canyon.json"), toggles.oceanCanyons());
        addIfEnabled(enabled, carverBase.resolve("mega_ravine.json"),
                modifierBase.resolve("add_mega_ravines.json"), toggles.megaRavines());
        addIfEnabled(enabled, carverBase.resolve("sinkhole.json"),
                modifierBase.resolve("add_sinkholes.json"), toggles.sinkholes());
        addIfEnabled(enabled, carverBase.resolve("ocean_blue_hole.json"),
                modifierBase.resolve("add_blue_holes.json"), toggles.blueHoles());
        return List.copyOf(enabled);
    }

    private static void addIfEnabled(List<Path> enabled, Path carver, Path modifier,
            boolean isEnabled) {
        if (!isEnabled) {
            return;
        }
        if (Files.exists(carver)) {
            enabled.add(carver);
        }
        if (Files.exists(modifier)) {
            enabled.add(modifier);
        }
    }

    public interface CarverToggles {
        boolean oceanCanyons();

        boolean megaRavines();

        boolean sinkholes();

        boolean blueHoles();

        static CarverToggles fromConfig(TheExpanseConfig config) {
            return new CarverToggles() {
                @Override
                public boolean oceanCanyons() {
                    return config.carverEnabled.get();
                }

                @Override
                public boolean megaRavines() {
                    return config.megaRavines.get();
                }

                @Override
                public boolean sinkholes() {
                    return config.sinkholes.get();
                }

                @Override
                public boolean blueHoles() {
                    return config.blueHoles.get();
                }
            };
        }

        static CarverToggles of(boolean oceanCanyons, boolean megaRavines, boolean sinkholes,
                boolean blueHoles) {
            return new CarverToggles() {
                @Override
                public boolean oceanCanyons() {
                    return oceanCanyons;
                }

                @Override
                public boolean megaRavines() {
                    return megaRavines;
                }

                @Override
                public boolean sinkholes() {
                    return sinkholes;
                }

                @Override
                public boolean blueHoles() {
                    return blueHoles;
                }
            };
        }
    }
}
