package com.theexpanse;

import com.theexpanse.data.worldgen.processor.TheExpanseProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(TheExpanse.MOD_ID)
public class TheExpanse {
    public static final String MOD_ID = "the_expanse";

    public TheExpanse(IEventBus modEventBus) {
        TheExpanseProcessors.register(modEventBus);

        // DEBUG: prove container is built
        System.out.println("[TheExpanse] Constructor hit");
        // Ensure mixin bootstrap is run
        com.theexpanse.bootstrap.MixinCompatBootstrap.init();
        System.out.println("[TheExpanse] Mod constructor called");

        // Attach our runtime debug logger
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        server.registryAccess()
              .registryOrThrow(Registries.LEVEL_STEM)
              .entrySet()
              .forEach(entry -> {
                  var stem = entry.getValue();
                  DimensionType type = stem.type().value();
                  ResourceLocation dimensionTypeId = stem.type().unwrapKey()
                      .map(ResourceKey::location)
                      .orElse(null);
                  ResourceLocation noise = null;
                  if (stem.generator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
                      noise = noiseGenerator.generatorSettings().unwrapKey()
                          .map(ResourceKey::location)
                          .orElse(null);
                  }
                  ResourceLocation displayId = dimensionTypeId != null ? dimensionTypeId : entry.getKey().location();
                  System.out.println("[TheExpanse][Debug] DimensionType " + displayId +
                      " -> noise_settings=" + (noise == null ? "null" : noise.toString()) +
                      " min_y=" + type.minY() +
                      " height=" + type.height() +
                      " logical_height=" + type.logicalHeight());
              });

        var access = server.registryAccess();

        // Dump density functions
        var dfRegistry = access.registryOrThrow(Registries.DENSITY_FUNCTION);
        dfRegistry.entrySet().forEach(e ->
            System.out.println("[TheExpanse][DF] " + e.getKey())
        );

        // Dump configured carvers
        var carverRegistry = access.registryOrThrow(Registries.CONFIGURED_CARVER);
        carverRegistry.entrySet().forEach(e ->
            System.out.println("[TheExpanse][Carver] " + e.getKey())
        );

        // Dump configured features
        var featureRegistry = access.registryOrThrow(Registries.CONFIGURED_FEATURE);
        featureRegistry.entrySet().forEach(e ->
            System.out.println("[TheExpanse][Feature] " + e.getKey())
        );
    }
}
