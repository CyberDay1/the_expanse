package com.yourorg.worldrise.vendor.litho;

import com.yourorg.worldrise.vendor.litho.config.ConfigHandler;
import com.yourorg.worldrise.vendor.litho.registry.LithostitchedBuiltInRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Mod class for Lithostitched on Fabric.
 */
public final class LithostitchedFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		ConfigHandler.load(FabricLoader.getInstance().getConfigDir().resolve("lithostitched.json"));
		LithostitchedBuiltInRegistries.init();
    }
}
