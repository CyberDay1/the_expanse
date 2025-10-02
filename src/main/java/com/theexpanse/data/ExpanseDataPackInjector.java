package com.theexpanse.data;

import com.theexpanse.TheExpanse;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = TheExpanse.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ExpanseDataPackInjector {
    private static final String BUILTIN_PACK_ID = "the_expanse_builtin";
    private static final Component BUILTIN_PACK_TITLE = Component.literal("The Expanse Builtin");
    private static final ResourceLocation BUILTIN_PACK_LOCATION = ResourceLocation.fromNamespaceAndPath(TheExpanse.MOD_ID, "the_expanse");

    private ExpanseDataPackInjector() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        ModList.get().getModContainerById(TheExpanse.MOD_ID).ifPresent(modContainer -> {
            var modInfo = modContainer.getModInfo();
            var packLocationInfo = new PackLocationInfo(
                    BUILTIN_PACK_ID,
                    BUILTIN_PACK_TITLE,
                    PackSource.BUILT_IN,
                    Optional.of(new KnownPack(TheExpanse.MOD_ID, BUILTIN_PACK_ID, modInfo.getVersion().toString()))
            );

            Path resourcePath = modInfo.getOwningFile().getFile().findResource(BUILTIN_PACK_LOCATION.getPath());
            if (resourcePath == null) {
                return;
            }

            event.addRepositorySource(consumer -> {
                Pack pack = Pack.readMetaAndCreate(
                        packLocationInfo,
                        BuiltInPackSource.fromName(info -> new PathPackResources(info, resourcePath)),
                        PackType.SERVER_DATA,
                        new PackSelectionConfig(true, Pack.Position.TOP, false)
                );

                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        });
    }
}
