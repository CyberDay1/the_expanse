package com.theexpanse.vendor.tectonic.api.exception;

import com.theexpanse.vendor.tectonic.api.depth.DepthTracker;


public class InvalidTemplateException extends ConfigException {
    public InvalidTemplateException(String message, DepthTracker tracker) {
        super(message, tracker);
    }
}
