package com.theexpanse.vendor.litho.access;

import com.theexpanse.vendor.litho.worldgen.structure.LithostitchedTemplates;

public interface StructurePoolAccess {
    LithostitchedTemplates getLithostitchedTemplates();
    void compileRawTemplates();
}