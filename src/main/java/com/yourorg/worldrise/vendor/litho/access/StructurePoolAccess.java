package com.yourorg.worldrise.vendor.litho.access;

import com.yourorg.worldrise.vendor.litho.worldgen.structure.LithostitchedTemplates;

public interface StructurePoolAccess {
    LithostitchedTemplates getLithostitchedTemplates();
    void compileRawTemplates();
}