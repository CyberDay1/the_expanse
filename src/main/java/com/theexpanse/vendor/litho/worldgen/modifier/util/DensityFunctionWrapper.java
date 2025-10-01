package com.theexpanse.vendor.litho.worldgen.modifier.util;

import com.theexpanse.vendor.litho.worldgen.densityfunction.MarkerFunction;
import com.theexpanse.vendor.litho.worldgen.densityfunction.MergedDensityFunction;
import com.theexpanse.vendor.litho.worldgen.densityfunction.OriginalMarkerDensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class DensityFunctionWrapper {
    public static DensityFunction wrap(final DensityFunction wrapped, DensityFunction wrapper) {
        if (wrapped instanceof MergedDensityFunction merged) {
            final DensityFunction original = merged.original();
            return new MergedDensityFunction(original, wrapped, wrapper.mapAll(value -> {
                if (isMarker(value)) {
                    if (value instanceof OriginalMarkerDensityFunction) {
                        return original;
                    }
                    return wrapped;
                }

                return value;
            }));
        }

        return new MergedDensityFunction(wrapped, wrapped, wrapper.mapAll(value -> {
            if (isMarker(value)) {
                return wrapped;
            }

            return value;
        }));
    }

    private static boolean isMarker(DensityFunction df) {
        return df instanceof DensityFunctions.HolderHolder hh && hh.function().value() instanceof MarkerFunction;
    }
}
