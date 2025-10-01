package com.theexpanse.vendor.tectonic.impl.loading.loaders.primitives;

import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.exception.LoadException;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.type.TypeLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedType;


public class ShortLoader implements TypeLoader<Short> {
    @Override
    public Short load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) {
        try {
            return ((Number) c).shortValue();
        } catch(ClassCastException e) {
            throw new LoadException("Data provided is not a short. Data is type: " + c.getClass().getSimpleName(), e, depthTracker);
        }
    }
}
