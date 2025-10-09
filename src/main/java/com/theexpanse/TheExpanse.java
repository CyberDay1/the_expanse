package com.theexpanse;

import net.neoforged.fml.common.Mod;

import com.theexpanse.worldgen.OreScaler;

@Mod(TheExpanse.MOD_ID)
public final class TheExpanse {
    public static final String MOD_ID = "the_expanse";

    public TheExpanse() {
        OreScaler.register();
    }
}
