package com.yourorg.worldrise;

import com.cyberday1.theexpanse.MixinCompatBootstrap;
import com.yourorg.worldrise.config.WorldriseConfig;
import com.yourorg.worldrise.datapack.WorldriseDatapackInjector;
import com.yourorg.worldrise.vendor.VendoredWorldgen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("worldrise")
public final class Worldrise {
    private static final Logger LOGGER = LoggerFactory.getLogger(Worldrise.class);

    public Worldrise(ModContainer container) {
        MixinCompatBootstrap.enforce();
        VendoredWorldgen.init(container);
        NeoForge.EVENT_BUS.addListener(WorldriseDatapackInjector::onServerAboutToStart);
        if (isMoonriseActive(WorldriseConfig.INSTANCE)) {
            LOGGER.info("Moonrise detected. Worldrise will avoid chunk pipeline interference.");
        }
    }

    public static boolean isMoonriseActive(WorldriseConfig config) {
        return config.moonriseCompatEnabled.get() && ModList.get().isLoaded("moonrise");
    }
}
