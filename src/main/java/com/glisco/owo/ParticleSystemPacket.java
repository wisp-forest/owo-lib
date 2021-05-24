package com.glisco.owo;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class ParticleSystemPacket {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Identifier ID = new Identifier("owo", "particles");

    static Packet<?> create(Identifier handler, BlockPos pos, Consumer<PacketByteBuf> dataProcessor) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeIdentifier(handler);
        buffer.writeBlockPos(pos);

        dataProcessor.accept(buffer);

        return ServerPlayNetworking.createS2CPacket(ID, buffer);
    }

    public static void onPacket(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {

        Identifier handlerID = packetByteBuf.readIdentifier();
        BlockPos pos = packetByteBuf.readBlockPos();

        ServerParticles.ParticlePacketHandler handler = ServerParticles.getHandler(handlerID);
        if (handler == null) {
            LOGGER.warn("Received particle packet for unknown handler \"" + handlerID + "\"");
            return;
        }

        handler.onPacket(minecraftClient, pos, packetByteBuf);
    }
}
