package io.wispforest.owo.util.pond;

import io.wispforest.owo.client.screens.MenuNetworkingInternals;
import net.minecraft.world.entity.player.Player;

public interface OwoAbstractContainerMenuExtension {
    void owo$attachToPlayer(Player player);

    void owo$readPropertySync(MenuNetworkingInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(MenuNetworkingInternals.LocalPacket packet, boolean clientbound);
}
