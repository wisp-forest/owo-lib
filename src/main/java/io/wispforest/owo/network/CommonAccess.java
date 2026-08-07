package io.wispforest.owo.network;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;

public interface CommonAccess
    <P extends Player, R extends ReentrantBlockableEventLoop, L extends PacketListener & PacketContextProvider>
    extends OwoNetChannel.EnvironmentAccess<P, R, L> {
    @Override
    default Connection connection() {
        return this.packetListener().getPacketContext().get(PacketContext.CONNECTION);
    }
}
