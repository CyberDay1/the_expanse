package com.theexpanse.expanse_heights.worldgen;

import com.cyberday1.expanseheights.ExpanseHeights;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ExpanseHeights.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public final class OreScaler {
    private static final int NEW_MIN = -256;
    private static final int NEW_MAX = 2000;
    private static final int VANILLA_RANGE = 384;
    private static final int VANILLA_MIN = -64;
    private static final int VANILLA_MAX = VANILLA_MIN + VANILLA_RANGE;

    private static final TagKey<PlacedFeature> ORE_TAG =
        TagKey.create(Registries.PLACED_FEATURE, new ResourceLocation("forge", "ores"));

    private OreScaler() {
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        final RegistryAccess access = event.getRegistryAccess();
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profiler) {
                scaleOrePlacements(access);
            }
        });
    }

    private static void scaleOrePlacements(RegistryAccess access) {
        Registry<PlacedFeature> registry = access.registryOrThrow(Registries.PLACED_FEATURE);
        Optional<HolderSet.Named<PlacedFeature>> ores = registry.getTag(ORE_TAG);
        if (ores.isEmpty()) {
            return;
        }

        for (Holder<PlacedFeature> holder : ores.get()) {
            if (!(holder instanceof Holder.Reference<PlacedFeature> reference)) {
                continue;
            }

            PlacedFeature feature = reference.value();
            List<PlacementModifier> modifiers = feature.placement();
            List<PlacementModifier> updated = new ArrayList<>(modifiers.size());
            boolean changed = false;

            for (PlacementModifier modifier : modifiers) {
                PlacementModifier scaled = scaleModifier(modifier);
                if (scaled != modifier) {
                    changed = true;
                }
                updated.add(scaled);
            }

            if (changed) {
                PlacedFeature replacement = new PlacedFeature(feature.feature(), List.copyOf(updated));
                reference.bindValue(replacement);
            }
        }
    }

    private static PlacementModifier scaleModifier(PlacementModifier modifier) {
        if (modifier instanceof HeightRangePlacement range) {
            HeightRangePlacement scaled = scaleHeightRange(range);
            return scaled != null ? scaled : modifier;
        }
        return modifier;
    }

    private static HeightRangePlacement scaleHeightRange(HeightRangePlacement original) {
        Optional<JsonElement> encoded = HeightRangePlacement.CODEC.encodeStart(JsonOps.INSTANCE, original).result();
        if (encoded.isEmpty()) {
            return null;
        }

        JsonElement json = encoded.get();
        if (!scaleAnchors(json)) {
            return null;
        }

        return HeightRangePlacement.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(null);
    }

    private static boolean scaleAnchors(JsonElement element) {
        if (element instanceof JsonObject object) {
            boolean changed = scaleAnchorObject(object);
            for (var entry : object.entrySet()) {
                if (scaleAnchors(entry.getValue())) {
                    changed = true;
                }
            }
            return changed;
        }

        if (element instanceof JsonArray array) {
            boolean changed = false;
            for (JsonElement value : array) {
                if (scaleAnchors(value)) {
                    changed = true;
                }
            }
            return changed;
        }

        return false;
    }

    private static boolean scaleAnchorObject(JsonObject object) {
        if (object.has("absolute")) {
            return replaceWithAbsolute(object, object.get("absolute"), 0);
        }

        if (object.has("above_bottom")) {
            return replaceWithAbsolute(object, object.get("above_bottom"), VANILLA_MIN);
        }

        if (object.has("below_top")) {
            JsonElement value = object.get("below_top");
            if (value != null && value.isJsonPrimitive()) {
                int absolute = VANILLA_MAX - value.getAsInt();
                return setAbsolute(object, absolute);
            }
            return false;
        }

        return false;
    }

    private static boolean replaceWithAbsolute(JsonObject object, JsonElement element, int offset) {
        if (element == null || !element.isJsonPrimitive()) {
            return false;
        }
        int absolute = offset + element.getAsInt();
        return setAbsolute(object, absolute);
    }

    private static boolean setAbsolute(JsonObject object, int value) {
        int scaled = scaleY(value);
        object.remove("absolute");
        object.remove("above_bottom");
        object.remove("below_top");
        object.addProperty("absolute", scaled);
        return true;
    }

    private static int scaleY(int originalY) {
        double scaled = (originalY / (double) VANILLA_RANGE) * NEW_MAX;
        int rounded = (int) Math.round(scaled);
        return Mth.clamp(rounded, NEW_MIN, NEW_MAX);
    }
}
