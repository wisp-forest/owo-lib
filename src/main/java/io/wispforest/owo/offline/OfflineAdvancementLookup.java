package io.wispforest.owo.offline;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.offline.AdvancementProgressAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Allows retrieving and editing the saved
 * advancement data of a player
 * <p>
 * <b>This only works while the given player is offline</b>
 *
 * @author BasiqueEvangelist
 */
public final class OfflineAdvancementLookup {

    private OfflineAdvancementLookup() {}

    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(Identifier.class, new Identifier.Serializer()).setPrettyPrinting().create();
    private static final TypeToken<Map<Identifier, AdvancementProgress>> JSON_TYPE = new TypeToken<>() {};

    /**
     * Saves the given advancement state
     * for the given player to disk
     *
     * @param player The player to modify
     * @param map    The advancement state to save
     */
    public static void put(UUID player, Map<Identifier, AdvancementProgress> map) {
        DataSavedEvents.ADVANCEMENTS.invoker().onSaved(player, map);

        try {
            Path advancementsPath = Owo.currentServer().getSavePath(WorldSavePath.ADVANCEMENTS);
            Path advancementPath = advancementsPath.resolve(player.toString() + ".json");
            JsonElement savedElement = GSON.toJsonTree(map);
            savedElement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());

            try (OutputStream os = Files.newOutputStream(advancementPath);
                 OutputStreamWriter osWriter = new OutputStreamWriter(os, Charsets.UTF_8.newEncoder())) {
                GSON.toJson(savedElement, osWriter);
            }

        } catch (IOException e) {
            Owo.LOGGER.error("Couldn't save advancements of offline player {}", player, e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Loads the advancement state
     * of the given player from disk
     *
     * @param player The player to query
     * @return The saved advancement data, or {@code null} if none is saved
     */
    public static @Nullable Map<Identifier, AdvancementProgress> get(UUID player) {
        try {
            Path advancementsPath = Owo.currentServer().getSavePath(WorldSavePath.ADVANCEMENTS);

            if (!Files.exists(advancementsPath))
                return null;

            Path advancementFile = advancementsPath.resolve(player + ".json");

            if (!Files.exists(advancementFile))
                return null;

            Dynamic<JsonElement> dynamic;
            try (InputStream s = Files.newInputStream(advancementFile);
                 InputStreamReader streamReader = new InputStreamReader(s);
                 JsonReader reader = new JsonReader(streamReader)) {
                reader.setLenient(false);
                dynamic = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(reader));
            }
            if (dynamic.get("DataVersion").asNumber().result().isEmpty()) {
                dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
            }

            dynamic = Schemas.getFixer().update(DataFixTypes.ADVANCEMENTS.getTypeReference(), dynamic, dynamic.get("DataVersion").asInt(0), SharedConstants.getGameVersion().getSaveVersion().getId());
            dynamic = dynamic.remove("DataVersion");

            Map<Identifier, AdvancementProgress> parsedMap = GSON.getAdapter(JSON_TYPE).fromJsonTree(dynamic.getValue());
            for (Map.Entry<Identifier, AdvancementProgress> entry : parsedMap.entrySet()) {
                if (((AdvancementProgressAccessor) entry.getValue()).getRequirements().length == 0) {
                    Advancement adv = Owo.currentServer().getAdvancementLoader().get(entry.getKey());

                    if (adv != null) {
                        ((AdvancementProgressAccessor) entry.getValue()).setRequirements(adv.getRequirements());
                    }
                }
            }

            return parsedMap;
        } catch (IOException e) {
            Owo.LOGGER.error("Couldn't get advancements for offline player {}", player, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Edits the saved advancement state of the given player
     * with the given editing function
     *
     * @param player The player to target
     * @param editor The function to apply to the advancement state
     */
    public static void edit(UUID player, Consumer<OfflineAdvancementState> editor) {
        Map<Identifier, AdvancementProgress> advancementData = get(player);
        if (advancementData == null) advancementData = new HashMap<>();

        var transaction = new OfflineAdvancementState(advancementData);
        editor.accept(transaction);
        put(player, transaction.advancementData());
    }

    /**
     * @return The UUID of every player that has saved advancements
     */
    public static List<UUID> savedPlayers() {
        Path advancementsPath = Owo.currentServer().getSavePath(WorldSavePath.ADVANCEMENTS);

        if (!Files.isDirectory(advancementsPath))
            return Collections.emptyList();

        List<UUID> list = new ArrayList<>();

        try {
            Iterator<Path> iterator = Files.list(advancementsPath).iterator();
            while (iterator.hasNext()) {
                Path savedPlayerFile = iterator.next();

                if (Files.isDirectory(savedPlayerFile) || !savedPlayerFile.toString().endsWith(".json")) {
                    continue;
                }

                try {
                    String filename = savedPlayerFile.getFileName().toString();
                    String uuidStr = filename.substring(0, filename.lastIndexOf('.'));
                    UUID uuid = UUID.fromString(uuidStr);
                    list.add(uuid);
                } catch (IllegalArgumentException iae) {
                    Owo.LOGGER.error("Encountered invalid UUID in advancements directory", iae);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
