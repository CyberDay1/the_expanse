package com.theexpanse.worldgen.carver;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.function.Function;

/**
 * Blue Hole vertical carver (NeoForge 1.21.1 compatible).
 */
public class BlueHoleCarver extends WorldCarver<CarverConfiguration> {

    public BlueHoleCarver(Codec<CarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CarverConfiguration config, RandomSource random) {
        // Controls how often this carver starts.
        // Returning true means it can start anywhere (for now).
        return random.nextFloat() < 0.01F; // 1% chance per chunk
    }

    @Override
    public boolean carve(
            CarvingContext context,
            CarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeFunc,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask mask
    ) {
        // Example placeholder: No carving yet
        return false;
    }
}
