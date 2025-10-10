package com.theexpanse;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.theexpanse.worldgen.OreScaler;
import com.theexpanse.worldgen.carver.CarverRegistry;

@Mod(TheExpanse.MOD_ID)
public final class TheExpanse {
    public static final String MOD_ID = "the_expanse";

    public TheExpanse(IEventBus modBus, ModContainer container) {
        CarverRegistry.register(modBus);
        OreScaler.register();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
