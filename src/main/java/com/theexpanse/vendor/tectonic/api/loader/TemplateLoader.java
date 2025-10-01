package com.theexpanse.vendor.tectonic.api.loader;

import com.theexpanse.vendor.tectonic.api.config.Configuration;
import com.theexpanse.vendor.tectonic.api.config.template.ConfigTemplate;
import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;


public interface TemplateLoader {
    <T extends ConfigTemplate> T load(T config, Configuration configuration, ValueLoader loader, DepthTracker depthTracker);
}
