package com.theexpanse.data.worldgen.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ApplyRandomProcessor extends StructureProcessor {
    public static final MapCodec<ApplyRandomProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.PER_PIECE).forGetter(ApplyRandomProcessor::mode),
            RegistryCodecs.homogeneousList(Registries.PROCESSOR_LIST).fieldOf("processor_lists").forGetter(ApplyRandomProcessor::processorLists)
        ).apply(instance, ApplyRandomProcessor::new)
    );

    private final Mode mode;
    private final HolderSet<StructureProcessorList> processorLists;

    public ApplyRandomProcessor(Mode mode, HolderSet<StructureProcessorList> processorLists) {
        this.mode = mode;
        this.processorLists = processorLists;
    }

    private Mode mode() {
        return mode;
    }

    private HolderSet<StructureProcessorList> processorLists() {
        return processorLists;
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
        Optional<Holder<StructureProcessorList>> selected = selectList(pos, pivot);
        if (selected.isEmpty()) {
            return current;
        }

        StructureTemplate.StructureBlockInfo result = current;
        for (StructureProcessor processor : selected.get().value().list()) {
            result = processor.process(level, pos, pivot, original, result, settings, template);
            if (result == null) {
                return null;
            }
        }

        return result;
    }

    private Optional<Holder<StructureProcessorList>> selectList(BlockPos pos, BlockPos pivot) {
        if (processorLists.size() == 0) {
            return Optional.empty();
        }

        RandomSource random = switch (mode) {
            case PER_BLOCK -> RandomSource.create(Mth.getSeed(pos));
            case PER_PIECE -> RandomSource.create(Mth.getSeed(pivot));
        };

        return processorLists.getRandomElement(random);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TheExpanseProcessors.APPLY_RANDOM.get();
    }

    public enum Mode implements StringRepresentable {
        PER_PIECE("per_piece"),
        PER_BLOCK("per_block");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
