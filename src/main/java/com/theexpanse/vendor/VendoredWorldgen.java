package com.theexpanse.vendor;

import com.theexpanse.config.TheExpanseConfig;
import com.theexpanse.vendor.litho.config.ConfigHandler;
import com.theexpanse.vendor.litho.registry.LithostitchedBuiltInRegistries;
import com.theexpanse.vendor.litho.worldgen.surface.SurfaceRuleManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;

public final class VendoredWorldgen {
    private VendoredWorldgen() {
    }

    public static void init(ModContainer container) {
        boolean tectonicEnabled = TheExpanseConfig.INSTANCE.tectonicEnabled.get();
        boolean lithoEnabled = TheExpanseConfig.INSTANCE.lithoEnabled.get();
        IEventBus bus = container.getEventBus();

        if (!tectonicEnabled && !lithoEnabled) {
            return;
        }

        if (lithoEnabled) {
            ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("lithostitched.json"));
            LithostitchedBuiltInRegistries.init(bus);
            SurfaceRuleManager.init();
        }

        if (tectonicEnabled) {
            // Reserved for future tectonic initialization.
        }
    }
}
