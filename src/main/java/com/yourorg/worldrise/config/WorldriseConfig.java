package com.yourorg.worldrise.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class WorldriseConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final WorldriseConfig INSTANCE;

    public final ModConfigSpec.BooleanValue oreScaling;
    public final ModConfigSpec.BooleanValue carverEnabled;
    public final ModConfigSpec.BooleanValue strongholdScaling;
    public final ModConfigSpec.BooleanValue ancientCityScaling;
    public final ModConfigSpec.BooleanValue mineshaftScaling;

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
        strongholdScaling = builder.comment("Enable height rescaling for strongholds")
                                   .define("strongholdScaling", true);
        ancientCityScaling = builder.comment("Enable height rescaling for ancient cities")
                                     .define("ancientCityScaling", true);
        mineshaftScaling = builder.comment("Enable height rescaling for mineshafts")
                                  .define("mineshaftScaling", true);
        builder.pop();
    }
}
