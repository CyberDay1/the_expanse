package com.theexpanse.mixin;

import org.spongepowered.asm.mixin.MixinEnvironment;

public class MixinCompatBootstrap {
    public static void init() {
        apply();
    }

    public static void apply() {
        MixinEnvironment.CompatibilityLevel target = MixinEnvironment.CompatibilityLevel.JAVA_21;
        if (MixinEnvironment.getCompatibilityLevel().compareTo(target) < 0) {
            MixinEnvironment.setCompatibilityLevel(target);
        }
    }
}
