package com.yourorg.worldrise.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public class WorldriseConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final WorldriseConfig INSTANCE;

    public final ModConfigSpec.BooleanValue oreScaling;
    public final ModConfigSpec.BooleanValue carverEnabled;
    public final ModConfigSpec.BooleanValue megaRavines;
    public final ModConfigSpec.BooleanValue sinkholes;
    public final ModConfigSpec.BooleanValue blueHoles;
    public final ModConfigSpec.DoubleValue defaultOreMultiplier;
    public final ModConfigSpec.DoubleValue defaultCarverMultiplier;
    public final ModConfigSpec.ConfigValue<List<? extends String>> biomeOreMultipliers;
    public final ModConfigSpec.ConfigValue<List<? extends String>> biomeCarverMultipliers;
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
    public final ModConfigSpec.BooleanValue biomesOPlentyCompat;
    public final ModConfigSpec.BooleanValue bygCompat;

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
        builder.push("scaling");
        defaultOreMultiplier = builder.comment("Default ore density multiplier (applied when no biome override matches)")
                                       .defineInRange("defaultOreMultiplier", 1.0, 0.0, 10.0);
        defaultCarverMultiplier = builder.comment("Default carver probability multiplier (applied when no biome override matches)")
                                          .defineInRange("defaultCarverMultiplier", 1.0, 0.0, 10.0);
        biomeOreMultipliers = builder.comment("Biome-specific ore multipliers")
                                     .defineList("biomeOreMultipliers", List.of(), o -> o instanceof String);
        biomeCarverMultipliers = builder.comment("Biome-specific carver multipliers")
                                        .defineList("biomeCarverMultipliers", List.of(), o -> o instanceof String);
        builder.pop();
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
        builder.push("compatibility");
        biomesOPlentyCompat = builder.comment("Enable Biomes O' Plenty biome compatibility")
                                     .define("biomesoplenty", true);
        bygCompat = builder.comment("Enable Oh The Biomes We've Gone biome compatibility")
                           .define("byg", true);
        builder.pop();
        builder.pop();
    }
}
