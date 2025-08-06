package io.wispforest.owo.network.neoforge;

import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.network.OwoHandshake;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static io.wispforest.owo.network.OwoNetChannel.MessagePayload;

public class NeoOwoNetworking {

    public static final Map<CustomPayload.Id<MessagePayload>, SidedPacketCodec<MessagePayload>> PAYLOAD_ID_TO_SIDED_CODEC = new HashMap<>();

    public static final Map<CustomPayload.Id<?>, PayloadCodec<?>> PAYLOAD_ID_TO_CLIENT_CODEC = new HashMap<>();
    public static final Map<CustomPayload.Id<?>, PayloadHandler<?>> PAYLOAD_ID_TO_CLIENT_HANDLER = new HashMap<>();

    public static final Map<CustomPayload.Id<MessagePayload>, PayloadHandler<MessagePayload>> PAYLOAD_ID_TO_SERVER_PAYLOAD_HANDLER = new HashMap<>();
    public static final Map<CustomPayload.Id<MessagePayload>, PayloadHandler<MessagePayload>> PAYLOAD_ID_TO_CLIENT_PAYLOAD_HANDLER = new HashMap<>();

    public static void onNetworkRegister(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.0.0");

        for (var entry : PAYLOAD_ID_TO_CLIENT_CODEC.entrySet()) {
            var id = entry.getKey();

            var handler = Objects.requireNonNull(PAYLOAD_ID_TO_CLIENT_HANDLER.get(id), "Unable to register the given client play packet due to missing the needed handler! Id: " + id);

            entry.getValue().registerPlayPayload(registrar, handler);
        }

        OwoHandshake.register(registrar);
        ScreenInternals.init(registrar);

        /* | \/ Optional Section Below \/ | */

        registrar = registrar.optional();

        for (var entry : PAYLOAD_ID_TO_SIDED_CODEC.entrySet()) {
            var id = entry.getKey();

            if (!PAYLOAD_ID_TO_SERVER_PAYLOAD_HANDLER.containsKey(id)) {
                throw new IllegalStateException("Unable to get the required Payload Handler as its missing for the Server! Id: " + id);
            } else if (!PAYLOAD_ID_TO_CLIENT_PAYLOAD_HANDLER.containsKey(id)) {
                throw new IllegalStateException("Unable to get the required Payload Handler as its missing for the Client! Id: " + id);
            }

            IPayloadHandler<MessagePayload> biDiHandler = (arg, iPayloadContext) -> {
                iPayloadContext.enqueueWork(() -> {
                    var player = iPayloadContext.player();

                    var handler = (!player.getWorld().isClient())
                        ? PAYLOAD_ID_TO_SERVER_PAYLOAD_HANDLER.get(id)
                        : PAYLOAD_ID_TO_CLIENT_PAYLOAD_HANDLER.get(id);

                    handler.accept(arg, iPayloadContext.player());
                });
            };

            registrar.playBidirectional(id, entry.getValue(), biDiHandler, biDiHandler);
        }
    }

    public static void registerMessageCodecs(CustomPayload.Id<MessagePayload> id, PacketCodec<PacketByteBuf, MessagePayload> serverCodec, PacketCodec<PacketByteBuf, MessagePayload> clientCodec) {
        if (PAYLOAD_ID_TO_SIDED_CODEC.containsKey(id)) {
            throw new IllegalStateException("Unable to register the given codec as such already exists within codec map! Id: " + id);
        }

        PAYLOAD_ID_TO_SIDED_CODEC.put(id, new SidedPacketCodec<>(serverCodec, clientCodec));
    }

    public static <T extends CustomPayload> void registerClientCodec(CustomPayload.Id<T> id, PacketCodec<PacketByteBuf, T> codec) {
        if (PAYLOAD_ID_TO_CLIENT_CODEC.containsKey(id)) {
            throw new IllegalStateException("Unable to register the given codec as such already exists within codec map! Id: " + id);
        }

        PAYLOAD_ID_TO_CLIENT_CODEC.put(id, new PayloadCodec<T>(id, codec, Optional.of(NetworkSide.CLIENTBOUND)));
    }

    public static <T extends CustomPayload> void registerClientPayload(CustomPayload.Id<T> id, PayloadHandler<T> payloadHandler) {
        if (PAYLOAD_ID_TO_CLIENT_HANDLER.containsKey(id)) {
            throw new IllegalStateException("Unable to register the given codec as such already exists within codec map! Id: " + id);
        }

        PAYLOAD_ID_TO_CLIENT_HANDLER.put(id, payloadHandler);
    }


    private record PayloadCodec<T extends CustomPayload>(CustomPayload.Id<T> id, PacketCodec<PacketByteBuf, T> codec, Optional<NetworkSide> possibleSide) {
        public void registerPlayPayload(PayloadRegistrar registrar, PayloadHandler<?> handler) {
            var castedHandler = (PayloadHandler<T>) handler;

            possibleSide.ifPresentOrElse(side -> {
                if (side.isClientbound()) {
                    registrar.playToClient(id, codec, (arg, context) -> context.enqueueWork(() -> castedHandler.accept(arg, context.player())));
                } else {
                    registrar.playToServer(id, codec, (arg, context) -> context.enqueueWork(() -> castedHandler.accept(arg, context.player())));
                }
            }, () -> {
                IPayloadHandler<T> biDiHandler = (arg, context) -> context.enqueueWork(() -> castedHandler.accept(arg, context.player()));

                registrar.playBidirectional(id, codec, biDiHandler, biDiHandler);
            });
        }
    }

    public static void registerServerMessageHandler(CustomPayload.Id<MessagePayload> id, PayloadHandler<MessagePayload> handler) {
        if (PAYLOAD_ID_TO_SERVER_PAYLOAD_HANDLER.containsKey(id)) {
            throw new IllegalStateException("Unable to register the given server handler as such already exists within handler map! Id: " + id);
        }

        PAYLOAD_ID_TO_SERVER_PAYLOAD_HANDLER.put(id, handler);
    }

    public static void registerClientMessageHandler(CustomPayload.Id<MessagePayload> id, PayloadHandler<MessagePayload> handler) {
        if (PAYLOAD_ID_TO_CLIENT_PAYLOAD_HANDLER.containsKey(id)) {
            throw new IllegalStateException("Unable to register the given client handler as such already exists within handler map! Id: " + id);
        }

        PAYLOAD_ID_TO_CLIENT_PAYLOAD_HANDLER.put(id, handler);
    }

    public interface PayloadHandler<T extends CustomPayload> extends BiConsumer<T, PlayerEntity> {
        static <P extends CustomPayload> PayloadHandler<P> empty() { return (payload, player) -> {}; }

        @Override void accept(T payload, PlayerEntity player);
    }
}
