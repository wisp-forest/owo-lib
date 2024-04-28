package io.wispforest.owo.util.pond;

import io.wispforest.owo.client.screens.ScreenInternals;
import net.minecraft.entity.player.PlayerEntity;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(PlayerEntity player);

    void owo$readPropertySync(ScreenInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(ScreenInternals.LocalPacket packet, boolean clientbound);
}
