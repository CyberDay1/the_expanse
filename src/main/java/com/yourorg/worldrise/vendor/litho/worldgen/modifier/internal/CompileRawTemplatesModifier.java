package com.yourorg.worldrise.vendor.litho.worldgen.modifier.internal;

import com.mojang.serialization.MapCodec;
import com.yourorg.worldrise.vendor.litho.access.StructurePoolAccess;
import com.yourorg.worldrise.vendor.litho.worldgen.modifier.Modifier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public record CompileRawTemplatesModifier() implements Modifier {

    public static final MapCodec<CompileRawTemplatesModifier> CODEC = MapCodec.unit(CompileRawTemplatesModifier::new);

    @Override
    public ModifierPhase getPhase() {
        return ModifierPhase.AFTER_ALL;
    }

    @Override
    public void applyModifier(RegistryAccess registries) {
        var poolRegistry = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        for (StructureTemplatePool pool : poolRegistry) {
            ((StructurePoolAccess)pool).compileRawTemplates();
        }
    }

    @Override
    public void applyModifier() {

    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}
