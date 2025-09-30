package com.yourorg.worldrise;

import com.cyberday1.theexpanse.MixinCompatBootstrap;
import com.yourorg.worldrise.vendor.VendoredWorldgen;
import net.neoforged.fml.common.Mod;

@Mod("worldrise")
public final class Worldrise {
    public Worldrise() {
        MixinCompatBootstrap.enforce();
        VendoredWorldgen.init();
    }
}
