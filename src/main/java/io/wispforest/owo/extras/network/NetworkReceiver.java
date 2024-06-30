package io.wispforest.owo.extras.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface NetworkReceiver<T> {
    void onPacket(T packet, Context ctx);

    record Context(@Nullable PlayerEntity player, PacketListener listener, Consumer<CustomPayload> responseSender){
        public void disconnect(Text text) {
            this.listener.onDisconnected(new DisconnectionInfo(text));
        }
    }

    static <T> NetworkReceiver<T> sidedReceiver(NetworkReceiver<T> client, NetworkReceiver<T> server) {
        return (packet, ctx) -> {
            if(ctx.listener.getSide() == NetworkSide.CLIENTBOUND) {
                client.onPacket(packet, ctx);
            } else {
                server.onPacket(packet, ctx);
            }
        };
    }

}
