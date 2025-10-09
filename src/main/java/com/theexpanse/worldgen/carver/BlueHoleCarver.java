package com.theexpanse.worldgen.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.function.Function;

public class BlueHoleCarver extends WorldCarver<CaveCarverConfiguration> {
    private static final int MIN_DEPTH = 50;
    private static final int DEPTH_VARIATION = 51;
    private static final int MIN_RADIUS = 9;
    private static final int RADIUS_VARIATION = 10;

    public BlueHoleCarver() {
        super(CaveCarverConfiguration.CODEC);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration cfg, RandomSource random) {
        return random.nextFloat() < cfg.probability;
    }

    @Override
    public boolean carve(
            CarvingContext context,
            CaveCarverConfiguration cfg,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomes,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask mask) {
        int centerX = chunkPos.getMinBlockX() + random.nextInt(16);
        int centerZ = chunkPos.getMinBlockZ() + random.nextInt(16);

        int seaLevel = context.randomState().surfaceSystem().getSeaLevel();
        int topY = Math.min(seaLevel + 5, context.getMinGenY() + context.getGenDepth() - 1);
        int depth = MIN_DEPTH + random.nextInt(DEPTH_VARIATION);
        int bottomY = Math.max(context.getMinGenY(), topY - depth);

        int radius = MIN_RADIUS + random.nextInt(RADIUS_VARIATION);
        int radiusSq = radius * radius;

        boolean carved = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; ++dx) {
            int x = centerX + dx;
            int localX = x - chunkPos.getMinBlockX();
            if (localX < 0 || localX >= 16) {
                continue;
            }

            int dxSq = dx * dx;

            for (int dz = -radius; dz <= radius; ++dz) {
                int z = centerZ + dz;
                int localZ = z - chunkPos.getMinBlockZ();
                if (localZ < 0 || localZ >= 16) {
                    continue;
                }

                if (dxSq + dz * dz > radiusSq) {
                    continue;
                }

                for (int y = topY; y >= bottomY; --y) {
                    int localY = y - context.getMinGenY();
                    if (localY < 0 || localY >= context.getGenDepth()) {
                        continue;
                    }

                    if (mask.get(localX, localY, localZ)) {
                        continue;
                    }

                    pos.set(x, y, z);
                    BlockState current = chunk.getBlockState(pos);
                    if (current.isAir()) {
                        continue;
                    }

                    mask.set(localX, localY, localZ);
                    BlockState replacement =
                            y <= seaLevel ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    chunk.setBlockState(pos, replacement, 2);
                    if (!replacement.getFluidState().isEmpty()) {
                        chunk.markPosForPostprocessing(pos);
                    }

                    carved = true;
                }
            }
        }

        return carved;
    }
}
