package com.theexpanse.worldgen.carver;

import com.theexpanse.TheExpanse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.carver.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CarverRegistry {
    private CarverRegistry() {}

    // Create the main carver registry for this mod
    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, TheExpanse.MOD_ID);

    // === Canyon-type carvers ===
    public static final DeferredHolder<WorldCarver<?>, WorldCarver<CanyonCarverConfiguration>> MEGA_OCEAN_RAVINE =
            CARVERS.register("mega_ocean_ravine", MegaOceanRavineCarver::new);

    public static final DeferredHolder<WorldCarver<?>, WorldCarver<CanyonCarverConfiguration>> MASSIVE_RAVINE =
            CARVERS.register("massive_ravine", MassiveRavineCarver::new);

    // === Custom “Blue Hole” vertical carver ===
    // Uses generic CarverConfiguration
    public static final DeferredHolder<WorldCarver<?>, WorldCarver<CarverConfiguration>> BLUE_HOLE =
            CARVERS.register("blue_hole", () -> new BlueHoleCarver(CarverConfiguration.CODEC.codec()));

    // === Registration hook ===
    public static void register(IEventBus modBus) {
        CARVERS.register(modBus);
    }

    // === Optional resource keys for configured JSON-based carvers ===
    public static final ResourceKey<ConfiguredWorldCarver<?>> RK_MEGA_OCEAN_RAVINE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, TheExpanse.id("mega_ocean_ravine"));

    public static final ResourceKey<ConfiguredWorldCarver<?>> RK_MASSIVE_RAVINE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, TheExpanse.id("massive_ravine"));

    public static final ResourceKey<ConfiguredWorldCarver<?>> RK_BLUE_HOLE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, TheExpanse.id("blue_hole"));
}
