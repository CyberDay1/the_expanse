package com.theexpanse.vendor.tectonic.api.preprocessor;

import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;


public interface ValuePreprocessor<A extends Annotation> {
    @NotNull <T> Result<T> process(AnnotatedType t, T c, ConfigLoader loader, A annotation, DepthTracker tracker);
}
