package com.theexpanse.vendor.tectonic.impl.loading.object;

import com.theexpanse.vendor.tectonic.api.config.template.object.ObjectTemplate;
import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.exception.LoadException;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.type.TypeLoader;
import com.theexpanse.vendor.tectonic.impl.MapConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedType;
import java.util.Map;
import java.util.function.Supplier;


@SuppressWarnings("unchecked")
public class ObjectTemplateLoader<T> implements TypeLoader<T> {
    private final Supplier<ObjectTemplate<T>> provider;

    public ObjectTemplateLoader(Supplier<ObjectTemplate<T>> provider) {
        this.provider = provider;
    }

    @Override
    public T load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker)
    throws LoadException {
        return loader.load(provider.get(), new MapConfiguration((Map<String, Object>) c), depthTracker).get();
    }
}
