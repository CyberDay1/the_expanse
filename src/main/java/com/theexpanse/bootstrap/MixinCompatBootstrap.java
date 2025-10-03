package com.theexpanse.bootstrap;

import org.spongepowered.asm.mixin.MixinEnvironment;

public final class MixinCompatBootstrap {
    private MixinCompatBootstrap() {}

    public static void init() {
        MixinEnvironment.CompatibilityLevel level = MixinEnvironment.CompatibilityLevel.JAVA_21;
        if (!MixinEnvironment.getCompatibilityLevel().equals(level)) {
            MixinEnvironment.setCompatibilityLevel(level);
            boolean success = MixinEnvironment.getCompatibilityLevel().equals(level);
            System.out.println("[TheExpanse] Forcing Mixin compatibility to JAVA_21 → " + success);
        } else {
            System.out.println("[TheExpanse] Mixin already at JAVA_21");
        }
    }
}
