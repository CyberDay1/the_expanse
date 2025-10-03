package com.theexpanse.data.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theexpanse.data.worldgen.processor.util.BlockStateUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class BlockSwapProcessor extends StructureProcessor {
    public static final MapCodec<BlockSwapProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), BuiltInRegistries.BLOCK.byNameCodec())
                .fieldOf("blocks")
                .forGetter(BlockSwapProcessor::blockMap)
        ).apply(instance, BlockSwapProcessor::new)
    );

    private final Map<Block, Block> blockMap;

    public BlockSwapProcessor(Map<Block, Block> blockMap) {
        this.blockMap = new Object2ObjectOpenHashMap<>(blockMap);
    }

    private Map<Block, Block> blockMap() {
        return blockMap;
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(
        LevelReader level,
        BlockPos pos,
        BlockPos pivot,
        StructureTemplate.StructureBlockInfo original,
        StructureTemplate.StructureBlockInfo current,
        StructurePlaceSettings settings,
        @Nullable StructureTemplate template
    ) {
        BlockState state = current.state();
        Block replacementBlock = blockMap.get(state.getBlock());
        if (replacementBlock == null || replacementBlock == state.getBlock()) {
            return current;
        }

        BlockState replacement = BlockStateUtil.copyProperties(state, replacementBlock.defaultBlockState());
        return new StructureTemplate.StructureBlockInfo(current.pos(), replacement, current.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TheExpanseProcessors.BLOCK_SWAP.get();
    }
}
