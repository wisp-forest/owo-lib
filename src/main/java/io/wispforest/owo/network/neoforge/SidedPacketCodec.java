package io.wispforest.owo.network.neoforge;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

// DUMMY CODEC TO HOLD SIDED CODECS
public record SidedPacketCodec<T>(PacketCodec<PacketByteBuf, T> serverCodec, PacketCodec<PacketByteBuf, T> clientCodec) implements PacketCodec<PacketByteBuf, T> {

    public PacketCodec<PacketByteBuf, T> getCodec(NetworkSide side) {
        return side == NetworkSide.CLIENTBOUND ? clientCodec : serverCodec;
    }

    @Override
    public T decode(PacketByteBuf buf) {
        throw new IllegalStateException("[owo] Sided Packet Codec has not been unpacked, issue has occured!");
    }

    @Override
    public void encode(PacketByteBuf buf, T value) {
        throw new IllegalStateException("[owo] Sided Packet Codec has not been unpacked, issue has occured!");
    }
}
