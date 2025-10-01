package com.theexpanse;

import org.spongepowered.asm.mixin.MixinEnvironment;

public class MixinCompatBootstrap {
    public static void enforce() {
        MixinEnvironment.getDefaultEnvironment()
            .setCompatibilityLevel(MixinEnvironment.CompatibilityLevel.valueOf("JAVA_21"));
    }
}
