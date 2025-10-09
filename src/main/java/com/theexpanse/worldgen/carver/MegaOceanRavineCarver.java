package com.theexpanse.worldgen.carver;

import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;

import java.util.Random;

public class MegaOceanRavineCarver extends CanyonWorldCarver {
    // 1.5x deeper, 1.25x wider; clamp floor to -75
    private static final float WIDTH_SCALE = 1.25f;
    private static final int MIN_FLOOR_Y = -75;

    public MegaOceanRavineCarver() {
        super(CanyonCarverConfiguration.CODEC);
    }

    @Override
    protected float getThickness(Random random) {
        // wider ravine
        return super.getThickness(random) * WIDTH_SCALE;
    }

    @Override
    protected double getTunnelSystemHeight(Random random) {
        // modestly deeper pathing
        return super.getTunnelSystemHeight(random) * 1.5;
    }

    @Override
    protected int getCanyonHeight(int seaLevel, Random random) {
        // deeper start height, but clamp to -75 min
        int h = super.getCanyonHeight(seaLevel, random);
        return Math.max(h, MIN_FLOOR_Y);
    }
}
