package com.yourorg.worldrise.vendor.litho;

import com.yourorg.worldrise.vendor.litho.config.ConfigHandler;
import com.yourorg.worldrise.vendor.litho.registry.LithostitchedBuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Mod class for Lithostitched on Forge.
 */
@Mod(LithostitchedCommon.MOD_ID)
public final class LithostitchedNeoforge {
	public LithostitchedNeoforge(IEventBus bus) {
		ConfigHandler.load(FMLPaths.CONFIGDIR.get().resolve("lithostitched.json"));
		LithostitchedBuiltInRegistries.init(bus);
	}
}
