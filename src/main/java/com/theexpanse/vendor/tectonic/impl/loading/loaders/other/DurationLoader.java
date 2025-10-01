package com.theexpanse.vendor.tectonic.impl.loading.loaders.other;

import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.type.TypeLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedType;
import java.time.Duration;


public class DurationLoader implements TypeLoader<Duration> {
    @Override
    public Duration load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) {
        return Duration.parse((String) c);
    }
}
