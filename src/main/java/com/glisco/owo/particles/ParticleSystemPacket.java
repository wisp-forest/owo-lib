package com.glisco.owo.particles;

import com.glisco.owo.util.VectorSerializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public class ParticleSystemPacket {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Identifier ID = new Identifier("owo", "particles");

    static Packet<?> create(Identifier handler, Vec3d pos, Consumer<PacketByteBuf> dataProcessor) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeIdentifier(handler);
        VectorSerializer.write(pos, buffer);

        dataProcessor.accept(buffer);

        return ServerPlayNetworking.createS2CPacket(ID, buffer);
    }

    public static void onPacket(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {

        Identifier handlerID = packetByteBuf.readIdentifier();
        Vec3d pos = VectorSerializer.read(packetByteBuf);

        ServerParticles.ParticlePacketHandler handler = ServerParticles.getHandler(handlerID);
        if (handler == null) {
            LOGGER.warn("Received particle packet for unknown handler \"" + handlerID + "\"");
            return;
        }

        handler.onPacket(minecraftClient, pos, packetByteBuf);
    }
}
