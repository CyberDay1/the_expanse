package com.theexpanse;

import com.theexpanse.config.TheExpanseConfig;
import com.theexpanse.vendor.VendoredWorldgen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TheExpanse.MOD_ID)
public final class TheExpanse {
    public static final String MOD_ID = "the_expanse";
    private static final Logger LOGGER = LoggerFactory.getLogger(TheExpanse.class);

    static {
        com.theexpanse.mixin.MixinCompatBootstrap.apply();
    }

    public TheExpanse(ModContainer container) {
        VendoredWorldgen.init(container);
        if (isMoonriseActive(TheExpanseConfig.INSTANCE)) {
            LOGGER.info("Moonrise detected. The Expanse will avoid chunk pipeline interference.");
        }
    }

    public static boolean isMoonriseActive(TheExpanseConfig config) {
        return config.moonriseCompatEnabled.get() && ModList.get().isLoaded("moonrise");
    }
}
