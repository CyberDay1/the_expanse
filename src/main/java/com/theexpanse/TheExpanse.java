package com.theexpanse;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.fml.common.Mod;

@Mod("the_expanse")
public class TheExpanse {
    public static final String MOD_ID = "the_expanse";

    public TheExpanse() {
        // DEBUG: prove container is built
        System.out.println("[TheExpanse] Constructor hit");
        // Ensure mixin bootstrap is run
        com.theexpanse.bootstrap.MixinCompatBootstrap.init();
        System.out.println("[TheExpanse] Mod constructor called");

        RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        Registry<DensityFunction> registry = registryAccess.registryOrThrow(Registries.DENSITY_FUNCTION);
        registry.entrySet().forEach(e -> {
            System.out.println("[TheExpanse][DF] " + e.getKey() + " -> " + e.getValue());
        });
    }
}
