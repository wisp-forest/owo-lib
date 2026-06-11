package io.wispforest.owo.neoforge.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SidedStreamCodec<T extends CustomPacketPayload>(StreamCodec<FriendlyByteBuf, T> serverCodec, StreamCodec<FriendlyByteBuf, T> clientCodec) implements StreamCodec<FriendlyByteBuf, T> {

    public StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> getCodec(PacketFlow flow) {
        return flow == PacketFlow.CLIENTBOUND ? clientCodec : serverCodec;
    }

    @Override
    public T decode(FriendlyByteBuf buf) {
        throw new IllegalStateException("[owo] Sided Packet Codec has not been unpacked, issue has occured!");
    }

    @Override
    public void encode(FriendlyByteBuf buf, T value) {
        throw new IllegalStateException("[owo] Sided Packet Codec has not been unpacked, issue has occured!");
    }
}
