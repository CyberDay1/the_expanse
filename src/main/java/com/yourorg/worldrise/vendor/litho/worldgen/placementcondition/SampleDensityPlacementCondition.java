package com.yourorg.worldrise.vendor.litho.worldgen.placementcondition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.yourorg.worldrise.vendor.litho.LithostitchedCommon;
import net.minecraft.core.BlockPos;

/**
 * Stub replacement for SampleDensityPlacementCondition.
 * Disabled under NeoForge: always returns false.
 */
public final class SampleDensityPlacementCondition implements PlacementCondition {
    private static final SampleDensityPlacementCondition INSTANCE = new SampleDensityPlacementCondition();

    public static final MapCodec<SampleDensityPlacementCondition> CODEC = MapCodec.unit(() -> INSTANCE);

    public static final Codec<SampleDensityPlacementCondition> DIRECT_CODEC = CODEC.codec();

    private static boolean warned;

    private SampleDensityPlacementCondition() {
    }

    @Override
    public boolean test(Context context, BlockPos pos) {
        if (!warned) {
            LithostitchedCommon.LOGGER.warn("SampleDensityPlacementCondition is disabled under NeoForge and will always return false.");
            warned = true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends PlacementCondition> codec() {
        return CODEC;
    }
}
