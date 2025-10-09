package com.theexpanse.worldgen.carver;

import com.theexpanse.TheExpanse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;

public final class CarverRegistry {
    private CarverRegistry() {}

    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, TheExpanse.MOD_ID);

    // canyon-derived
    public static final RegistryObject<WorldCarver<CanyonCarverConfiguration>> MEGA_OCEAN_RAVINE =
            CARVERS.register("mega_ocean_ravine", MegaOceanRavineCarver::new);

    public static final RegistryObject<WorldCarver<CanyonCarverConfiguration>> MASSIVE_RAVINE =
            CARVERS.register("massive_ravine", MassiveRavineCarver::new);

    // custom vertical “blue hole”
    public static final RegistryObject<WorldCarver<CaveCarverConfiguration>> BLUE_HOLE =
            CARVERS.register("blue_hole", BlueHoleCarver::new);

    public static void register(IEventBus modBus) {
        CARVERS.register(modBus);
    }

    // Optional resource keys for configured instances (JSONs will reference these)
    public static final ResourceKey<ConfiguredWorldCarver<?>> RK_MEGA_OCEAN_RAVINE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, TheExpanse.id("mega_ocean_ravine"));

    public static final ResourceKey<ConfiguredWorldCarver<?>> RK_MASSIVE_RAVINE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, TheExpanse.id("massive_ravine"));

    public static final ResourceKey<ConfiguredWorldCarver<?>> RK_BLUE_HOLE =
            ResourceKey.create(Registries.CONFIGURED_CARVER, TheExpanse.id("blue_hole"));
}
