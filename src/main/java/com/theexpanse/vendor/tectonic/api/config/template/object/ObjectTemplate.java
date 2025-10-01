package com.theexpanse.vendor.tectonic.api.config.template.object;

import com.theexpanse.vendor.tectonic.api.config.template.ConfigTemplate;


/**
 * ConfigTemplate implementation representing an object, intended to be used in config loading.
 *
 * @param <T> Object type
 */
public interface ObjectTemplate<T> extends ConfigTemplate {
    T get();
}
