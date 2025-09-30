package com.yourorg.worldrise.vendor.litho.registry;

import com.mojang.serialization.MapCodec;
import com.yourorg.worldrise.vendor.litho.LithostitchedCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class LithostitchedMaterialRules {
    public static final ResourceKey<MapCodec<? extends SurfaceRules.RuleSource>> TRANSIENT_MERGED = LithostitchedCommon.createResourceKey(Registries.MATERIAL_RULE, "transient_merged");

}
