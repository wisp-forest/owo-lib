package io.wispforest.owo.util.pond;

import io.wispforest.owo.client.screens.ScreenInternals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(PlayerEntity player);

    void owo$readPropertySync(ScreenInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(ScreenInternals.LocalPacket packet, boolean clientbound);

    default void owo$sendToServer(CustomPayload payload) {
        // Do note that a mixin will prevent this exception on the client
        throw new IllegalStateException("Unable to execute owo$sendToServer as currently its not a CLIENT Dist!");
    }
}
