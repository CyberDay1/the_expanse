package com.theexpanse.vendor.tectonic.impl.loading.loaders;

import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.type.TypeLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedType;


/**
 * Default loader for String type.
 */
public class StringLoader implements TypeLoader<String> {
    @Override
    public String load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) {
        return (String) c;
    }
}
