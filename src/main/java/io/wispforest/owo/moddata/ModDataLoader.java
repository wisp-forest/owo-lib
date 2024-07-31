package io.wispforest.owo.moddata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.wispforest.owo.Owo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the logic to load JSON from all other mods' data directories
 * when {@link #load(ModDataConsumer)} is called. This should ideally be done
 * one and in a {@link net.fabricmc.api.ModInitializer}
 */
public final class ModDataLoader {

    private static final Gson GSON = new Gson();

    private static final Path DATA_PATH = FabricLoader.getInstance().getGameDir().resolve("moddata");

    private ModDataLoader() {}

    /**
     * Loads the data the {@code consumer} requests
     *
     * @param consumer The consumer to load data for
     */
    public static void load(ModDataConsumer consumer) {
        Map<Identifier, JsonObject> foundFiles = new HashMap<>();

        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            for (var rootPath : modContainer.getRootPaths()) {
                final var targetPath = rootPath.resolve(String.format("data/%s/%s", modContainer.getMetadata().getId(), consumer.getDataSubdirectory()));
                tryLoadFilesFrom(foundFiles, modContainer.getMetadata().getId(), targetPath);
            }
        });

        try {
            Files.createDirectories(DATA_PATH);

            try (var stream = Files.list(DATA_PATH)) {
                stream.forEach(nsPath -> {
                    if (!Files.isDirectory(nsPath)) return;

                    var namespace = nsPath.getFileName().toString();
                    var targetPath = nsPath.resolve(consumer.getDataSubdirectory());
                    if (!Files.exists(targetPath)) return;

                    tryLoadFilesFrom(foundFiles, namespace, targetPath);
                });
            }
        } catch (IOException e) {
            Owo.LOGGER.error("### Unable to traverse global data tree ++ Stacktrace below ###", e);
        }

        foundFiles.forEach(consumer::acceptParsedFile);
    }

    private static void tryLoadFilesFrom(Map<Identifier, JsonObject> foundFiles, String namespace, Path targetPath) {
        try {
            if (!Files.exists(targetPath)) return;

            try (var stream = Files.walk(targetPath)) {
                stream.forEach(path -> {
                    if (!path.toString().endsWith(".json")) return;
                    try {
                        final InputStreamReader tabData = new InputStreamReader(Files.newInputStream(path));

                        foundFiles.put(Identifier.of(namespace, FilenameUtils.removeExtension(targetPath.relativize(path).toString())), GSON.fromJson(tabData, JsonObject.class));
                    } catch (IOException e) {
                        Owo.LOGGER.warn("### Unable to open data file {} ++ Stacktrace below ###", path, e);
                    }
                });
            }

        } catch (IOException e) {
            Owo.LOGGER.error("### Unable to traverse data tree {} ++ Stacktrace below ###", targetPath, e);
        }
    }
}
