package com.yourorg.worldrise.vendor;

import com.yourorg.worldrise.config.WorldriseConfig;
import com.yourorg.worldrise.vendor.litho.config.ConfigHandler;
import com.yourorg.worldrise.vendor.litho.registry.LithostitchedBuiltInRegistries;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;

public final class VendoredWorldgen {
    private VendoredWorldgen() {
    }

    public static void init() {
        boolean tectonicEnabled = WorldriseConfig.INSTANCE.tectonicEnabled.get();
        boolean lithoEnabled = WorldriseConfig.INSTANCE.lithoEnabled.get();

        if (!tectonicEnabled && !lithoEnabled) {
            return;
        }

        if (lithoEnabled) {
            ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("lithostitched.json"));
            LithostitchedBuiltInRegistries.init(FMLJavaModLoadingContext.get().getModEventBus());
        }

        if (tectonicEnabled) {
            // Reserved for future tectonic initialization.
        }
    }
}
