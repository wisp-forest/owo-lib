package io.wispforest.owo.util.pond;

import io.wispforest.owo.client.screens.ScreenInternals;
import net.minecraft.world.entity.player.Player;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(Player player);

    void owo$readPropertySync(ScreenInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(ScreenInternals.LocalPacket packet, boolean clientbound);
}
