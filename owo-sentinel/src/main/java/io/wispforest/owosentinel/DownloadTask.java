package io.wispforest.owosentinel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

import javax.swing.*;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class DownloadTask extends SwingWorker<Void, Void> {

    private static final Gson GSON = new Gson();
    private final Runnable whenDone;
    private final Consumer<String> logger;

    public DownloadTask(Consumer<String> logger, Runnable whenDone) {
        this.logger = logger;
        this.whenDone = whenDone;
    }

    @Override
    protected void done() {
        whenDone.run();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Void doInBackground() throws Exception {
        logger.accept("Fetching versions");
        final URL url = new URL("https://api.modrinth.com/api/v1/mod/ccKDOlHs/version");

        final var response = GSON.fromJson(new InputStreamReader(url.openStream()), JsonArray.class);

        JsonObject latestVersion = null;

        versionLoop:
        for (var version : response) {
            final var versionObject = version.getAsJsonObject();
            for (var gameVersion : versionObject.get("game_versions").getAsJsonArray()) {
                if (!gameVersion.getAsString().equals(SharedConstants.VERSION_NAME)) continue;
                latestVersion = versionObject;
                break versionLoop;
            }
        }

        if (latestVersion != null) {
            final var firstFile = latestVersion
                    .get("files").getAsJsonArray().get(0).getAsJsonObject();

            final var versionHash = firstFile
                    .get("hashes").getAsJsonObject()
                    .get("sha512").getAsString();

            final var versionFilename = firstFile
                    .get("filename").getAsString();

            logger.accept("Found latest version: " + latestVersion.get("version_number").getAsString());

            final var filePath = FabricLoader.getInstance().getGameDir().resolve("mods").resolve(versionFilename);
            final var versionUrl = new URL("https://api.modrinth.com/api/v1/version_file/" + versionHash + "/download?algorithm=sha512");

            logger.accept("Downloading...");

            try (final var modStream = versionUrl.openStream()) {
                Files.copy(modStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            logger.accept("Success!");
        } else {
            logger.accept("No matching version found");
        }

        return null;
    }
}
