package com.glisco.owo.moddata;

import com.glisco.owo.Owo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * Contains the logic to load JSON from all other mods' data directory
 * when {@link #load(ModDataConsumer)} is called. This should ideally be done
 * one and in a {@link net.fabricmc.api.ModInitializer}
 */
public class ModDataLoader {

    private static final Gson GSON = new Gson();

    /**
     * Loads the data the {@code consumer} requests
     *
     * @param consumer The consumer to load data for
     */
    public static void load(ModDataConsumer consumer) {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            try {
                final var targetPath = modContainer.getRootPath().resolve(String.format("data/%s/%s", modContainer.getMetadata().getId(), consumer.getDataSubdirectory()));

                if (!Files.exists(targetPath)) return;
                Files.walk(targetPath).forEach(path -> {
                    if (!path.toString().endsWith(".json")) return;
                    try {
                        final InputStreamReader tabData = new InputStreamReader(Files.newInputStream(path));
                        consumer.acceptParsedFile(GSON.fromJson(tabData, JsonObject.class));
                    } catch (IOException e) {
                        Owo.LOGGER.warn("### Unable to open mod data file ++ Stacktrace below ###");
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                Owo.LOGGER.error("### Unable to traverse mod data tree ++ Stacktrace below ###");
                e.printStackTrace();
            }
        });
    }

}
