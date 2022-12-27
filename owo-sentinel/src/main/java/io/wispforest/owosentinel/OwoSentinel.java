package io.wispforest.owosentinel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class OwoSentinel {
    public static final Logger LOGGER = LogManager.getLogger("oωo-sentinel");
    private static final Gson GSON = new Gson();

    public static final String OWO_EXPLANATION = """
            oωo-lib is a library used by most mods under the
            Wisp Forest domain to ease development. This is
            simply a convenient installer, as oωo is missing from your
            installation. Should you not trust it, feel free to head to the
            repository and download oωo yourself.
            """;

    public static final boolean FORCE_HEADLESS = Boolean.getBoolean("owo.sentinel.forceHeadless");

    public static void launch() {
        if (FabricLoader.getInstance().isModLoaded("owo-impl")) return;

        try {
            if (GraphicsEnvironment.isHeadless() || FORCE_HEADLESS || System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
                SentinelConsole.run();
            } else {
                SentinelWindow.open();
            }
        } catch (Exception e) {
            LOGGER.error("Error thrown while opening sentinel! Exiting", e);
            System.exit(1);
        }

        System.exit(0);
    }

    public static List<String> listOwoDependents() {
        var list = new ArrayList<String>();

        for (var mod : FabricLoader.getInstance().getAllMods()) {
            for (var dependency : mod.getMetadata().getDependencies()) {
                if (!dependency.getModId().equals("owo") && !dependency.getModId().equals("owo-lib")) continue;
                list.add(mod.getMetadata().getName());
            }
        }

        return list;
    }

    @SuppressWarnings("deprecation")
    public static void downloadAndInstall(Consumer<String> logger) throws Exception {
        logger.accept("Fetching versions");
        final URL url = new URL("https://api.modrinth.com/v2/project/owo-lib/version?game_versions=[%22" + SharedConstants.VERSION_NAME + "%22]&loaders=[%22fabric%22]");

        final var response = GSON.fromJson(new InputStreamReader(url.openStream()), JsonArray.class);

        final var targetVersion = FabricLoader.getInstance().getModContainer("owo-sentinel").orElseThrow().getMetadata().getVersion().getFriendlyString();

        JsonObject latestVersion = null;

        for (var version : response) {
            final var versionObject = version.getAsJsonObject();

            if (versionObject.get("version_number").getAsString().equals(targetVersion)) {
                latestVersion = versionObject;
                break;
            }
        }

        if (latestVersion != null) {
            final var firstFile = latestVersion
                    .get("files").getAsJsonArray().get(0).getAsJsonObject();

            final var versionUrl = firstFile
                    .get("url").getAsString();

            final var versionFilename = firstFile
                    .get("filename").getAsString();

            logger.accept("Found matching version: " + latestVersion.get("version_number").getAsString());

            final var filePath = FabricLoader.getInstance().getGameDir().resolve("mods").resolve(versionFilename);

            logger.accept("Downloading...");

            try (final var modStream = new URL(versionUrl).openStream()) {
                Files.copy(modStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            logger.accept("Success!");
        } else {
            logger.accept("No matching version found");
        }
    }
}
