package io.wispforest.owo.neoforge.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jspecify.annotations.Nullable;

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

    public static <C extends CustomPacketPayload, S extends CustomPacketPayload> PayloadRegistrar configurationBiDirectional(PayloadRegistrar registrar,
                                                                                                                             Identifier id,
                                                                                                                             Class<S> serverPacketClass,
                                                                                                                             Class<C> clientPacketClass,
                                                                                                                             StreamCodec<FriendlyByteBuf, S> serverCodec,
                                                                                                                             StreamCodec<FriendlyByteBuf, C> clientCodec,
                                                                                                                             IPayloadHandler<S> serverHandler) {
        return configurationBiDirectional(registrar, id, serverPacketClass, clientPacketClass, serverCodec, clientCodec, serverHandler, null);
    }

    public static <C extends CustomPacketPayload, S extends CustomPacketPayload> PayloadRegistrar configurationBiDirectional(PayloadRegistrar registrar,
                                                                                                                             Identifier id,
                                                                                                                             Class<S> serverPacketClass,
                                                                                                                             Class<C> clientPacketClass,
                                                                                                                             StreamCodec<FriendlyByteBuf, S> serverCodec,
                                                                                                                             StreamCodec<FriendlyByteBuf, C> clientCodec,
                                                                                                                             IPayloadHandler<S> serverHandler,
                                                                                                                             @Nullable IPayloadHandler<C> clientHandler) {
        return registrar.configurationBidirectional(
            new CustomPacketPayload.Type<>(id),
            new SidedStreamCodec<CustomPacketPayload>((StreamCodec) serverCodec, (StreamCodec) clientCodec),
            (var1, var2) -> {
                if (!serverPacketClass.isInstance(var1)) {
                    if (clientPacketClass.isInstance(var1)){
                        throw new IllegalStateException("Unable to handle packet as it was found to be the client type of: " + clientPacketClass.getName());
                    } else {
                        throw new IllegalStateException("Unable to handle packet as it not the server type of: " + serverPacketClass.getName());
                    }
                }
                serverHandler.handle((S) var1, var2);
            },
            clientHandler != null
                ? (var1, var2) -> {
                if (!clientPacketClass.isInstance(var1)) {
                    if (serverPacketClass.isInstance(var1)){
                        throw new IllegalStateException("Unable to handle packet as it was found to be the client type of: " + serverPacketClass.getName());
                    } else {
                        throw new IllegalStateException("Unable to handle packet as it not the server type of: " + clientPacketClass.getName());
                    }
                }
                clientHandler.handle((C) var1, var2);
            }
                : null
        );
    }
}
