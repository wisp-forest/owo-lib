package io.wispforest.owo.util.pond;

import io.wispforest.owo.client.screens.ScreenInternals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.LinkedHashSet;
import java.util.Set;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(PlayerEntity player);

    void owo$readPropertySync(ScreenInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(ScreenInternals.LocalPacket packet, boolean clientbound);

    void owo$verifyData(ServerPlayerEntity player, Set<String> clientMessageNames);

    LinkedHashSet<String> owo$gatherMessageNames();
}
