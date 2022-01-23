package io.wispforest.owo.offline;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

/**
 * Events that get fired when the data
 * of a player is about to be saved, allowing
 * the data to be modified before it's written to disk
 *
 * @author BasiqueEvangelist
 */
public interface DataSavedEvents {

    /**
     * Called when the player data of a given player is about to be saved
     */
    Event<PlayerData> PLAYER_DATA = EventFactory.createArrayBacked(PlayerData.class, callbacks -> (playerUuid, newTag) -> {
        for (PlayerData callback : callbacks) {
            callback.onSaved(playerUuid, newTag);
        }
    });

    interface PlayerData {
        void onSaved(UUID playerUuid, NbtCompound newTag);
    }

    /**
     * Called when the advancements of a given player are about to be saved
     */
    Event<Advancements> ADVANCEMENTS = EventFactory.createArrayBacked(Advancements.class, callbacks -> (playerUuid, newMap) -> {
        for (Advancements callback : callbacks) {
            callback.onSaved(playerUuid, newMap);
        }
    });

    interface Advancements {
        void onSaved(UUID playerUuid, Map<Identifier, AdvancementProgress> newMap);
    }
}
