package com.yourorg.worldrise.datapack;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class WorldriseDatapackInjector {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldriseDatapackInjector.class);
    private static final String RESOURCE_ROOT = "/data/minecraft/";
    private static final String PACK_FOLDER_NAME = "Worldrise injected worldgen overrides";
    private static final List<String> PACK_RESOURCES = List.of(
            "pack.mcmeta",
            "dimension_type/overworld.json",
            "dimension_type/nether.json",
            "dimension_type/end.json",
            "worldgen/noise_settings/overworld.json",
            "worldgen/noise_settings/nether.json",
            "worldgen/noise_settings/end.json"
    );

    private WorldriseDatapackInjector() {
    }

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        Path datapackDir = server.getWorldPath(LevelResource.DATAPACK_DIR);
        copyIfMissing(datapackDir);
    }

    public static void copyIfMissing(Path datapackDir) {
        Path datapackRoot = datapackDir.resolve(PACK_FOLDER_NAME);
        if (Files.exists(datapackRoot)) {
            return;
        }

        try {
            Files.createDirectories(datapackRoot);
            for (String relativePath : PACK_RESOURCES) {
                Path destination = datapackRoot.resolve(relativePath);
                Path parent = destination.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                try (InputStream inputStream = WorldriseDatapackInjector.class.getResourceAsStream(RESOURCE_ROOT + relativePath)) {
                    if (inputStream == null) {
                        LOGGER.warn("Missing datapack resource: {}", relativePath);
                        continue;
                    }

                    Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ioException) {
                    LOGGER.error("Failed to copy datapack resource {}", relativePath, ioException);
                }
            }

            LOGGER.info("Injected Worldrise datapack at {}", datapackRoot);
        } catch (IOException exception) {
            LOGGER.error("Failed to prepare datapack directory {}", datapackDir, exception);
        }
    }
}
