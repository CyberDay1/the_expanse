package com.theexpanse;

import net.neoforged.fml.common.Mod;

@Mod("the_expanse")
public class TheExpanse {
    public static final String MOD_ID = "the_expanse";

    public TheExpanse() {
        // DEBUG: prove container is built
        System.out.println("[TheExpanse] Constructor hit");
        // Ensure mixin bootstrap is run
        com.theexpanse.mixin.MixinCompatBootstrap.init();
        System.out.println("[TheExpanse] Mod constructor called");
    }
}
