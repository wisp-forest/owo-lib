package io.wispforest.owo.offline;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

public interface PlayerAdvancementsSaved {
    Event<PlayerAdvancementsSaved> EVENT = EventFactory.createArrayBacked(PlayerAdvancementsSaved.class, callbacks -> (playerUuid, newMap) -> {
        for (PlayerAdvancementsSaved callback : callbacks) {
            callback.onPlayerAdvancementsSaved(playerUuid, newMap);
        }
    });

    void onPlayerAdvancementsSaved(UUID playerUuid, Map<Identifier, AdvancementProgress> newMap);
}
