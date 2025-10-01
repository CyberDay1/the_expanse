package com.theexpanse.mixin;

import org.spongepowered.asm.mixin.MixinEnvironment;

public class MixinCompatBootstrap {
    public static void apply() {
        MixinEnvironment.CompatibilityLevel level = MixinEnvironment.CompatibilityLevel.JAVA_21;
        if (MixinEnvironment.getCompatibilityLevel().compareTo(level) < 0) {
            MixinEnvironment.setCompatibilityLevel(level);
        }
    }
}
