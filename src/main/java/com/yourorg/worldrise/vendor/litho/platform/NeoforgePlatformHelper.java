package com.yourorg.worldrise.vendor.litho.platform;

import com.yourorg.worldrise.vendor.litho.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;

public class NeoforgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }
}
