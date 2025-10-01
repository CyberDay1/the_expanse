package com.theexpanse.vendor.litho.worldgen.surface;

import com.theexpanse.vendor.litho.LithostitchedCommon;

/**
 * Stub replacement for SurfaceRuleManager.
 * Disabled under NeoForge: no-op.
 */
public final class SurfaceRuleManager {
    private static boolean warned;

    private SurfaceRuleManager() {
    }

    public static void init() {
        if (!warned) {
            LithostitchedCommon.LOGGER.warn("SurfaceRuleManager is disabled under NeoForge; surface rule modifications will not be applied.");
            warned = true;
        }
    }
}
