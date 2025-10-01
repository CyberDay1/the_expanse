package com.theexpanse.data;

import com.theexpanse.config.TheExpanseConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility responsible for determining which dimension overrides are active based on the
 * configuration toggles. Each enabled dimension contributes both its dimension type definition
 * and the corresponding vanilla dimension override.
 */
public final class DimensionLoader {

    private static final Path DIMENSION_TYPE_ROOT =
            Path.of("data", "the-expanse", "dimension_type");
    private static final Path DIMENSION_ROOT = Path.of("data", "minecraft", "dimension");

    private DimensionLoader() { }

    /**
     * Resolves the dimension override files that should be loaded for the current configuration.
     *
     * @param resourceRoot root folder that contains the mod data pack resources
     * @return immutable list of absolute paths to dimension overrides that should be applied
     */
    public static List<Path> enabledDimensions(Path resourceRoot) {
        return enabledDimensions(resourceRoot,
                DimensionToggles.fromConfig(TheExpanseConfig.INSTANCE));
    }

    static List<Path> enabledDimensions(Path resourceRoot, DimensionToggles toggles) {
        Path dimensionTypeBase = resourceRoot.resolve(DIMENSION_TYPE_ROOT);
        Path dimensionBase = resourceRoot.resolve(DIMENSION_ROOT);

        List<Path> enabled = new ArrayList<>();
        addIfEnabled(enabled, dimensionTypeBase.resolve("nether.json"),
                dimensionBase.resolve("the_nether.json"), toggles.netherScaling());
        addIfEnabled(enabled, dimensionTypeBase.resolve("end.json"),
                dimensionBase.resolve("the_end.json"), toggles.endScaling());
        return List.copyOf(enabled);
    }

    private static void addIfEnabled(List<Path> enabled, Path dimensionType, Path dimension,
            boolean isEnabled) {
        if (!isEnabled) {
            return;
        }
        if (Files.exists(dimensionType)) {
            enabled.add(dimensionType);
        }
        if (Files.exists(dimension)) {
            enabled.add(dimension);
        }
    }

    public interface DimensionToggles {
        boolean netherScaling();

        boolean endScaling();

        static DimensionToggles fromConfig(TheExpanseConfig config) {
            return new DimensionToggles() {
                @Override
                public boolean netherScaling() {
                    return config.netherScaling.get();
                }

                @Override
                public boolean endScaling() {
                    return config.endScaling.get();
                }
            };
        }

        static DimensionToggles of(boolean nether, boolean end) {
            return new DimensionToggles() {
                @Override
                public boolean netherScaling() {
                    return nether;
                }

                @Override
                public boolean endScaling() {
                    return end;
                }
            };
        }
    }
}
