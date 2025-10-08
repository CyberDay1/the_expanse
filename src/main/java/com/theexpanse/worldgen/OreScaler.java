package com.theexpanse.worldgen;

import com.theexpanse.TheExpanse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
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
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = TheExpanse.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OreScaler {
    private static final int NEW_MIN = -256;
    private static final int NEW_MAX = 2288;
    private static final int VANILLA_RANGE = 384;
    private static final int VANILLA_MIN = -64;
    private static final int VANILLA_MAX = VANILLA_MIN + VANILLA_RANGE;

    private static final TagKey<PlacedFeature> ORE_TAG =
        TagKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath("forge", "ores"));

    private static final MethodHandle BIND_VALUE = findBindValueHandle();

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
                bindReferenceValue(reference, replacement);
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
        Codec<HeightRangePlacement> codec = HeightRangePlacement.CODEC.codec();
        Optional<JsonElement> encoded = codec.encodeStart(JsonOps.INSTANCE, original).result();
        if (encoded.isEmpty()) {
            return null;
        }

        JsonElement json = encoded.get();
        if (!scaleAnchors(json)) {
            return null;
        }

        return codec.parse(JsonOps.INSTANCE, json).result().orElse(null);
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

    private static MethodHandle findBindValueHandle() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                Holder.Reference.class, MethodHandles.lookup());
            MethodType signature = MethodType.methodType(void.class, Object.class);
            return lookup.findVirtual(Holder.Reference.class, "bindValue", signature);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new IllegalStateException("Failed to access Holder.Reference#bindValue", ex);
        }
    }

    private static void bindReferenceValue(Holder.Reference<PlacedFeature> reference, PlacedFeature replacement) {
        try {
            BIND_VALUE.invokeExact(reference, (Object) replacement);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unable to rebind placed feature", throwable);
        }
    }
}
