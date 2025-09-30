package com.yourorg.worldrise.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class WorldriseConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final WorldriseConfig INSTANCE;

    public final ModConfigSpec.BooleanValue oreScaling;
    public final ModConfigSpec.BooleanValue carverEnabled;

    static {
        final var builder = new ModConfigSpec.Builder();
        INSTANCE = new WorldriseConfig(builder);
        COMMON_SPEC = builder.build();
    }

    private WorldriseConfig(ModConfigSpec.Builder builder) {
        builder.push("worldrise");
        oreScaling = builder.comment("Enable rescaling of ore placements")
                            .define("oreScaling", true);
        carverEnabled = builder.comment("Enable custom ocean canyon carver")
                               .define("carverEnabled", true);
        builder.pop();
    }
}
