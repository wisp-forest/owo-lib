package io.wispforest.owo.network;

import io.wispforest.owo.mixin.neoforge.ClientAccessMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

///
/// This given class in neoforge will be modified with a client only self mixin as here [ClientAccessMixin] meaning such
/// will only allow for access in client environments.
///
public class ClientAccess implements OwoNetChannel.EnvironmentAccess<LocalPlayer, Minecraft, ClientPacketListener> {

    //@OnlyIn(Dist.CLIENT) private final ClientPacketListener packetListener;
    //@OnlyIn(Dist.CLIENT) private final Minecraft instance = Minecraft.getInstance();

    public ClientAccess(PlayerEntity player) {
        //this.netHandler = netHandler;
    }

    @Override
    //@OnlyIn(Dist.CLIENT)
    public LocalPlayer player() {
        throw new IllegalStateException("Unable to get player as such has not been permitted for Server Env");
    }

    @Override
    //@OnlyIn(Dist.CLIENT)
    public Minecraft runtime() {
        throw new IllegalStateException("Unable to get runtime as such has not been permitted for Server Env");
    }

    @Override
    //@OnlyIn(Dist.CLIENT)
    public ClientPlayNetworkHandler netHandler() {
        throw new IllegalStateException("Unable to get netHandler as such has not been permitted for Server Env");
    }
}
