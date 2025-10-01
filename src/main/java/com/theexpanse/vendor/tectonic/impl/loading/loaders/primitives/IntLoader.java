package com.theexpanse.vendor.tectonic.impl.loading.loaders.primitives;

import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.exception.LoadException;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.type.TypeLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedType;


/**
 * Default loader for Integer types.
 */
public class IntLoader implements TypeLoader<Integer> {
    @Override
    public Integer load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) {
        try {
            return ((Number) c).intValue();
        } catch(ClassCastException e) {
            throw new LoadException("Data provided is not an integer. Data is type: " + c.getClass().getSimpleName(), e, depthTracker);
        }
    }
}
