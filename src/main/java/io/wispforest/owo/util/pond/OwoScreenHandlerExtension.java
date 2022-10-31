package io.wispforest.owo.util.pond;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(ServerPlayerEntity player);

    void owo$readPropertySync(PacketByteBuf buf);
}
