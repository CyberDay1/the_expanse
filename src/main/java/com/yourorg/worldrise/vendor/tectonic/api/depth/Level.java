package com.yourorg.worldrise.vendor.tectonic.api.depth;

public interface Level {
    String descriptor();

    String joinDescriptor();

    default String verboseDescriptor() {
        return descriptor();
    }
}
