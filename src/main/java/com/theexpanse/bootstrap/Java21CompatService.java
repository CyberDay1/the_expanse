package com.theexpanse.bootstrap;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;
import java.util.Set;

public class Java21CompatService implements ITransformationService {
    @Override
    public String name() {
        return "the_expanse_java21_compat";
    }

    @Override
    public void initialize(IEnvironment environment) {
        try {
            MixinEnvironment.CompatibilityLevel target = MixinEnvironment.CompatibilityLevel.JAVA_21;
            if (MixinEnvironment.getCompatibilityLevel().compareTo(target) < 0) {
                MixinEnvironment.setCompatibilityLevel(target);
            }
        } catch (Throwable t) {
            // ignore; MixinEnvironment may not be initialized yet
        }
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
        // no-op
    }

    @Override
    public List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
