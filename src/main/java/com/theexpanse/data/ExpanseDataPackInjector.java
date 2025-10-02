package com.theexpanse.data;

import com.theexpanse.TheExpanse;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = TheExpanse.MOD_ID)
public final class ExpanseDataPackInjector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BUILTIN_PACK_ID = "mod/" + TheExpanse.MOD_ID;
    private static final Component BUILTIN_PACK_TITLE = Component.literal("The Expanse Datapack");

    private ExpanseDataPackInjector() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        ModList.get().getModContainerById(TheExpanse.MOD_ID).ifPresentOrElse(container -> {
            var modInfo = container.getModInfo();
            var packLocationInfo = new PackLocationInfo(
                    BUILTIN_PACK_ID,
                    BUILTIN_PACK_TITLE,
                    PackSource.BUILT_IN,
                    Optional.of(new KnownPack(TheExpanse.MOD_ID, BUILTIN_PACK_ID, modInfo.getVersion().toString()))
            );

            ResourcePackLoader.getPackFor(TheExpanse.MOD_ID).ifPresentOrElse(resourcesSupplier -> {
                event.addRepositorySource(repository -> {
                    var pack = Pack.readMetaAndCreate(
                            packLocationInfo,
                            resourcesSupplier,
                            PackType.SERVER_DATA,
                            new PackSelectionConfig(true, Pack.Position.TOP, false)
                    );

                    if (pack != null) {
                        repository.accept(pack);
                        LOGGER.info("Injected The Expanse builtin datapack at TOP priority");
                    } else {
                        LOGGER.warn("Failed to create The Expanse builtin datapack pack instance");
                    }
                });
            }, () -> LOGGER.warn("The Expanse pack resources were not discovered; builtin datapack will not be registered"));
        }, () -> LOGGER.warn("The Expanse mod container was not found; builtin datapack will not be registered"));
    }
}
