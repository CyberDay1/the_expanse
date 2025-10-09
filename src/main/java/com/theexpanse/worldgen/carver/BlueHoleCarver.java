package com.theexpanse.worldgen.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

public class BlueHoleCarver extends WorldCarver<CaveCarverConfiguration> {
    // Large vertical cylinder, 50–100 blocks deep; diameter ~18–28 blocks
    public BlueHoleCarver() {
        super(CaveCarverConfiguration.CODEC);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration cfg, Random random) {
        // Low probability; actual placement restricted via biome modifier to warm/lukewarm/deep oceans
        return random.nextFloat() < cfg.probability;
    }

    @Override
    protected boolean carveRegion(CarverDebugSettings dbg,
                                  CaveCarverConfiguration cfg,
                                  WorldGenLevel level,
                                  Function<BlockPos, Aquifer> aquifer,
                                  BlockPos chunkPos,
                                  Random random,
                                  int seaLevel,
                                  int chunkX, int chunkZ,
                                  int chunkMinY, int chunkHeight,
                                  BitSet carvingMask) {
        // Center somewhere in this chunk
        int centerX = chunkPos.getX() + random.nextInt(16);
        int centerZ = chunkPos.getZ() + random.nextInt(16);

        int depth = 50 + random.nextInt(51); // 50–100
        int topY = Math.min(seaLevel + 5, chunkMinY + chunkHeight - 1);
        int bottomY = Math.max(topY - depth, chunkMinY);

        int radius = 9 + random.nextInt(10); // diameter 18–28
        int r2 = radius * radius;

        boolean carved = false;

        for (int y = topY; y >= bottomY; y--) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int xx = centerX + dx;
                    int zz = centerZ + dz;
                    int dist2 = dx * dx + dz * dz;
                    if (dist2 <= r2) {
                        carved |= carveBlock(level, aquifer.apply(new BlockPos(xx, y, zz)), new BlockPos(xx, y, zz), true, cfg);
                    }
                }
            }
        }

        return carved;
    }

    private boolean carveBlock(WorldGenLevel level, Aquifer aquifer, BlockPos pos, boolean waterOk, CaveCarverConfiguration cfg) {
        if (!level.isInWorldBounds(pos)) return false;
        var state = level.getBlockState(pos);
        if (state.isAir()) return false;

        // Remove blocks to create the vertical shaft; leave water where appropriate
        if (waterOk && pos.getY() <= level.getSeaLevel()) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
        return true;
    }
}
