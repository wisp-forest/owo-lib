package io.wispforest.owo.offline;

import io.wispforest.owo.Owo;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class OfflineDataLookup {
    private OfflineDataLookup() {

    }

    public static void save(UUID player, NbtCompound tag) {
        PlayerDataSaved.EVENT.invoker().onPlayerDataSaved(player, tag);

        try {
            File savedPlayersPath = Owo.SERVER.getSavePath(WorldSavePath.PLAYERDATA).toFile();
            File file = File.createTempFile(player.toString() + "-", ".dat", savedPlayersPath);
            NbtIo.writeCompressed(tag, file);
            File newDataFile = new File(savedPlayersPath, player + ".dat");
            File oldDataFile = new File(savedPlayersPath, player + ".dat_old");
            Util.backupAndReplace(newDataFile, file, oldDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound get(UUID player) {
        try {
            Path savedPlayersPath = Owo.SERVER.getSavePath(WorldSavePath.PLAYERDATA);
            Path savedDataPath = savedPlayersPath.resolve(player.toString() + ".dat");
            NbtCompound rawTag = NbtIo.readCompressed(savedDataPath.toFile());
            int dataVersion = rawTag.contains("DataVersion", 3) ? rawTag.getInt("DataVersion") : -1;
            return NbtHelper.update(Schemas.getFixer(), DataFixTypes.PLAYER, rawTag, dataVersion);
        } catch (IOException e) {
            Owo.LOGGER.error("Couldn't get player data for offline player {}", player, e);
            throw new RuntimeException(e);
        }
    }

    public static List<UUID> listSavedPlayers() {
        List<UUID> list = new ArrayList<>();
        Path savedPlayersPath = Owo.SERVER.getSavePath(WorldSavePath.PLAYERDATA);

        if (!Files.isDirectory(savedPlayersPath))
            return Collections.emptyList();

        try {
            Iterator<Path> iter = Files.list(savedPlayersPath).iterator();
            while(iter.hasNext()) {
                Path savedPlayerFile = iter.next();

                if (Files.isDirectory(savedPlayerFile) || !savedPlayerFile.toString().endsWith(".dat")) {
                    continue;
                }

                try {
                    String filename = savedPlayerFile.getFileName().toString();
                    String uuidStr = filename.substring(0, filename.lastIndexOf('.'));
                    UUID uuid = UUID.fromString(uuidStr);
                    list.add(uuid);
                } catch (IllegalArgumentException iae) {
                    Owo.LOGGER.error("Encountered invalid UUID in playerdata directory! ", iae);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
