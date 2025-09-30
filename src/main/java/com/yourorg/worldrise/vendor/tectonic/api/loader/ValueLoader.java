package com.yourorg.worldrise.vendor.tectonic.api.loader;

import com.yourorg.worldrise.vendor.tectonic.api.config.Configuration;
import com.yourorg.worldrise.vendor.tectonic.api.depth.DepthTracker;

import java.lang.reflect.AnnotatedType;


@FunctionalInterface
public interface ValueLoader {
    default Object load(String key, AnnotatedType type, Configuration configuration, DepthTracker depthTracker) {
        return load(key, type, configuration, depthTracker, false);
    }

    Object load(String key, AnnotatedType type, Configuration configuration, DepthTracker depthTracker, boolean isFinal);
}
