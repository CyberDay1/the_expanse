package com.theexpanse.vendor.tectonic.api.config.template;

import com.theexpanse.vendor.tectonic.api.config.template.annotations.Value;
import com.theexpanse.vendor.tectonic.api.loader.AbstractConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.ConfigLoader;
import com.theexpanse.vendor.tectonic.api.loader.TemplateLoader;
import com.theexpanse.vendor.tectonic.impl.loading.template.ReflectiveTemplateLoader;


/**
 * Interface to be implemented by classes containing annotated fields to be loaded by
 * a {@link ConfigLoader}.
 *
 * @see Value
 * @see ConfigLoader
 * @see AbstractConfigLoader
 * @see ValidatedConfigTemplate
 */
public interface ConfigTemplate {
    default TemplateLoader loader() {
        return new ReflectiveTemplateLoader();
    }
}
