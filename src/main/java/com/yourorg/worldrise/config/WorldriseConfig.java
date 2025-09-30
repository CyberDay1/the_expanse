package com.yourorg.worldrise.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class WorldriseConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final WorldriseConfig INSTANCE;

    public final ModConfigSpec.BooleanValue oreScaling;
    public final ModConfigSpec.BooleanValue carverEnabled;
    public final ModConfigSpec.BooleanValue megaRavines;
    public final ModConfigSpec.BooleanValue sinkholes;
    public final ModConfigSpec.BooleanValue blueHoles;
    public final ModConfigSpec.DoubleValue oreDensityMultiplier;
    public final ModConfigSpec.DoubleValue carverChanceMultiplier;
    public final ModConfigSpec.BooleanValue strongholdScaling;
    public final ModConfigSpec.BooleanValue ancientCityScaling;
    public final ModConfigSpec.BooleanValue mineshaftScaling;
    public final ModConfigSpec.BooleanValue fortressScaling;
    public final ModConfigSpec.BooleanValue bastionScaling;
    public final ModConfigSpec.BooleanValue monumentScaling;
    public final ModConfigSpec.BooleanValue endCityScaling;
    public final ModConfigSpec.BooleanValue netherScaling;
    public final ModConfigSpec.BooleanValue endScaling;
    public final ModConfigSpec.BooleanValue tectonicEnabled;
    public final ModConfigSpec.BooleanValue lithoEnabled;

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
        megaRavines = builder.comment("Enable mega ravine carver")
                             .define("megaRavines", true);
        sinkholes = builder.comment("Enable surface sinkhole carver")
                           .define("sinkholes", true);
        blueHoles = builder.comment("Enable ocean blue hole carver")
                           .define("blueHoles", true);
        oreDensityMultiplier = builder.comment("Multiplier for ore density (default 1.0)")
                                      .defineInRange("oreDensityMultiplier", 1.0, 0.1, 10.0);
        carverChanceMultiplier = builder.comment("Multiplier for carver chance (default 1.0)")
                                        .defineInRange("carverChanceMultiplier", 1.0, 0.1, 10.0);
        strongholdScaling = builder.comment("Enable height rescaling for strongholds")
                                   .define("strongholdScaling", true);
        ancientCityScaling = builder.comment("Enable height rescaling for ancient cities")
                                     .define("ancientCityScaling", true);
        mineshaftScaling = builder.comment("Enable height rescaling for mineshafts")
                                  .define("mineshaftScaling", true);
        fortressScaling = builder.comment("Enable height rescaling for nether fortresses")
                                 .define("fortressScaling", true);
        bastionScaling = builder.comment("Enable height rescaling for bastion remnants")
                                .define("bastionScaling", true);
        monumentScaling = builder.comment("Enable height rescaling for ocean monuments")
                                 .define("monumentScaling", true);
        endCityScaling = builder.comment("Enable height rescaling for end cities")
                                .define("endCityScaling", true);
        netherScaling = builder.comment("Enable expanded nether vertical range")
                               .define("netherScaling", true);
        endScaling = builder.comment("Enable expanded end vertical range")
                            .define("endScaling", true);
        tectonicEnabled = builder.comment("Enable vendored tectonic configuration loader")
                                 .define("tectonicEnabled", true);
        lithoEnabled = builder.comment("Enable vendored lithostitched registry hooks")
                               .define("lithoEnabled", true);
        builder.pop();
    }
}
