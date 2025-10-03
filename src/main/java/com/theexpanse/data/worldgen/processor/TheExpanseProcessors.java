package com.theexpanse.data.worldgen.processor;

import com.theexpanse.TheExpanse;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public final class TheExpanseProcessors {
    private TheExpanseProcessors() {}

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
        DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TheExpanse.MOD_ID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BlockSwapProcessor>> BLOCK_SWAP =
        PROCESSORS.register("block_swap", () -> () -> BlockSwapProcessor.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<ApplyRandomProcessor>> APPLY_RANDOM =
        PROCESSORS.register("apply_random", () -> () -> ApplyRandomProcessor.CODEC);

    public static void register(IEventBus modEventBus) {
        PROCESSORS.register(modEventBus);
    }
}
