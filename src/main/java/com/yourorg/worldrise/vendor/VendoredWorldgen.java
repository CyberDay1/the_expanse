package com.yourorg.worldrise.vendor;

import com.yourorg.worldrise.config.WorldriseConfig;
import com.yourorg.worldrise.vendor.litho.config.ConfigHandler;
import com.yourorg.worldrise.vendor.litho.registry.LithostitchedBuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;

public final class VendoredWorldgen {
    private VendoredWorldgen() {
    }

    public static void init(ModContainer container) {
        boolean tectonicEnabled = WorldriseConfig.INSTANCE.tectonicEnabled.get();
        boolean lithoEnabled = WorldriseConfig.INSTANCE.lithoEnabled.get();
        IEventBus bus = container.getEventBus();

        if (!tectonicEnabled && !lithoEnabled) {
            return;
        }

        if (lithoEnabled) {
            ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("lithostitched.json"));
            LithostitchedBuiltInRegistries.init(bus);
        }

        if (tectonicEnabled) {
            // Reserved for future tectonic initialization.
        }
    }
}
