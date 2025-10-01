package com.theexpanse.vendor.tectonic.api.config.template.dynamic;

import com.theexpanse.vendor.tectonic.api.config.Configuration;
import com.theexpanse.vendor.tectonic.api.config.template.ConfigTemplate;
import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;
import com.theexpanse.vendor.tectonic.api.exception.ValueMissingException;
import com.theexpanse.vendor.tectonic.api.loader.TemplateLoader;
import com.theexpanse.vendor.tectonic.api.loader.ValueLoader;

import java.util.Map;


public class DynamicTemplateLoader implements TemplateLoader {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ConfigTemplate> T load(T config, Configuration configuration, ValueLoader loader, DepthTracker depthTracker) {
        if(!(config instanceof DynamicTemplate)) {
            throw new IllegalArgumentException("Must receive DynamicTemplate, got " + config.getClass().getCanonicalName());
        }

        DynamicTemplate dynamicTemplate = (DynamicTemplate) config;
        Map<String, Object> computed = (Map<String, Object>) dynamicTemplate.getComputed();

        dynamicTemplate.getValues().forEach((id, value) -> {
            try {
                computed.put(id, loader.load(value.getKey(), value.getAnnotatedType(), configuration, depthTracker.entry(value.getKey()),
                    value.isFinal()));
            } catch(ValueMissingException e) {
                if(value.isDefault()) {
                    computed.put(id, value.getDefaultValue());
                } else throw e;
            }
        });

        return config;
    }
}
