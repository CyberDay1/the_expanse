package com.theexpanse.worldgen.carver;

import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;

import java.util.Random;

public class MassiveRavineCarver extends CanyonWorldCarver {
    // 2x wider, ~4x longer via path step multiplier
    private static final float WIDTH_SCALE = 2.0f;
    private static final float PATH_STEP_SCALE = 4.0f;

    public MassiveRavineCarver() {
        super(CanyonCarverConfiguration.CODEC);
    }

    @Override
    protected float getThickness(Random random) {
        return super.getThickness(random) * WIDTH_SCALE;
    }

    @Override
    protected int getMaxCanyonLength() {
        // vanilla length * 4 for “massive”
        return (int) (super.getMaxCanyonLength() * PATH_STEP_SCALE);
    }
}
