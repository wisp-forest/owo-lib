package io.wispforest.owo.util.pond;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.concurrent.Executor;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(PlayerEntity player);

    void owo$readPropertySync(PacketByteBuf buf);

    void owo$handlePacket(PacketByteBuf buf, boolean clientbound, Executor executor);
}
