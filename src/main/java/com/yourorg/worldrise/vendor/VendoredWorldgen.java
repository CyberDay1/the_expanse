package com.yourorg.worldrise.vendor;

import com.yourorg.worldrise.vendor.litho.config.ConfigHandler;
import com.yourorg.worldrise.vendor.litho.registry.LithostitchedBuiltInRegistries;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;

public final class VendoredWorldgen {
    private VendoredWorldgen() {
    }

    public static void init() {
        ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("lithostitched.json"));
        LithostitchedBuiltInRegistries.init(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
