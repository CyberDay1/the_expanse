package com.yourorg.worldrise.vendor.tectonic.api.loader;

import com.yourorg.worldrise.vendor.tectonic.api.config.Configuration;
import com.yourorg.worldrise.vendor.tectonic.api.config.template.ConfigTemplate;
import com.yourorg.worldrise.vendor.tectonic.api.depth.DepthTracker;


public interface TemplateLoader {
    <T extends ConfigTemplate> T load(T config, Configuration configuration, ValueLoader loader, DepthTracker depthTracker);
}
