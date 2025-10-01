package com.yourorg.worldrise;

import com.cyberday1.theexpanse.MixinCompatBootstrap;
import com.yourorg.worldrise.config.WorldriseConfig;
import com.yourorg.worldrise.vendor.VendoredWorldgen;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Worldrise.MOD_ID)
public final class Worldrise {
    public static final String MOD_ID = "worldrise";
    private static final String BUILTIN_PACK_ID = "mod/" + MOD_ID;
    private static final Component BUILTIN_PACK_TITLE = Component.literal("Worldrise Datapack");
    private static final Logger LOGGER = LoggerFactory.getLogger(Worldrise.class);

    private final ModContainer modContainer;

    public Worldrise(ModContainer container) {
        this.modContainer = container;
        MixinCompatBootstrap.enforce();
        VendoredWorldgen.init(container);
        NeoForge.EVENT_BUS.addListener(this::registerBuiltinDatapack);
        if (isMoonriseActive(WorldriseConfig.INSTANCE)) {
            LOGGER.info("Moonrise detected. Worldrise will avoid chunk pipeline interference.");
        }
    }

    public static boolean isMoonriseActive(WorldriseConfig config) {
        return config.moonriseCompatEnabled.get() && ModList.get().isLoaded("moonrise");
    }

    private void registerBuiltinDatapack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        var modInfo = modContainer.getModInfo();
        var packLocationInfo = new PackLocationInfo(
                BUILTIN_PACK_ID,
                BUILTIN_PACK_TITLE,
                PackSource.BUILT_IN,
                Optional.of(new KnownPack(MOD_ID, BUILTIN_PACK_ID, modInfo.getVersion().toString()))
        );
        ResourcePackLoader.getPackFor(MOD_ID).ifPresentOrElse(resourcesSupplier -> {
            event.addRepositorySource(repository -> {
                var pack = Pack.readMetaAndCreate(
                        packLocationInfo,
                        resourcesSupplier,
                        PackType.SERVER_DATA,
                        new PackSelectionConfig(true, Pack.Position.TOP, false)
                );

                if (pack != null) {
                    repository.accept(pack);
                } else {
                    LOGGER.warn("Failed to create Worldrise builtin datapack pack instance");
                }
            });
        }, () -> LOGGER.warn("Worldrise pack resources were not discovered; builtin datapack will not be registered"));
    }
}
