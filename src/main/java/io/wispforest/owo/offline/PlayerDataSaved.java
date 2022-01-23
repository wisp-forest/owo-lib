package io.wispforest.owo.offline;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public interface PlayerDataSaved {
    Event<PlayerDataSaved> EVENT = EventFactory.createArrayBacked(PlayerDataSaved.class, callbacks -> (playerUuid, newTag) -> {
        for (PlayerDataSaved callback : callbacks) {
            callback.onPlayerDataSaved(playerUuid, newTag);
        }
    });

    void onPlayerDataSaved(UUID playerUuid, NbtCompound newTag);
}
