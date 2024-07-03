package io.wispforest.owo.extras.network;

import com.mojang.logging.LogUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttribute;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.owo.Owo;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class OwoInternalNetworking {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final OwoInternalNetworking INSTANCE = new OwoInternalNetworking();

    private final Map<NetworkConfiguration, Map<CustomPayload.Id<?>, Endec<?>>> payloadCodecs = new HashMap<>();
    private final Map<NetworkConfiguration, Map<CustomPayload.Id<?>, NetworkReceiver<?>>> receivers = new HashMap<>();

    @Nullable
    public PayloadRegistrar registrar = null;

    private static boolean freezeNetworkRegistration = false;

    public void initializeNetworking(final RegisterPayloadHandlersEvent event) {
        freezeNetworkRegistration = true;
        this.registrar = event.registrar("owo");

        this.receivers.forEach((configuration, receivers) -> {
            var payloadCodecs = this.payloadCodecs.getOrDefault(configuration, new HashMap<>());

            receivers.forEach((id, receiver) -> {
                var endec = payloadCodecs.remove(id);

                if(endec == null) {
                    throw new IllegalStateException("Unable to get the required codec to serialize the given packet: [Id: " + id + "]");
                }

                registerForConfiguration(configuration, id, toCodec(endec), receiver);
            });
        });

        this.payloadCodecs.forEach((configuration, packetCodecs) -> {
            var payloadCodecs = this.payloadCodecs.getOrDefault(configuration, Map.of());

            for (var id : payloadCodecs.keySet()) {
                var codec = toCodec(payloadCodecs.remove(id));
                registerForConfiguration(configuration, id, codec, (packet, context) -> {});
            }
        });
    }

    private void registerForConfiguration(NetworkConfiguration networkConfiguration, CustomPayload.Id<?> id, PacketCodec<? super PacketByteBuf, ?> codec, NetworkReceiver<?> receiver) {
        var phase = networkConfiguration.phase;

        switch (networkConfiguration.direction) {
            case C2S -> {
                switch (phase) {
                    case PLAY -> register(registrar::playToServer, id, codec, receiver);
                    case CONFIGURATION -> register(registrar::configurationToServer, id, codec, receiver);
                    default -> throw new IllegalStateException("Unable to handle the given phase for the following packet: [Id: " + id + ", Phase: " + phase + "]");
                }
            }
            case S2C -> {
                switch (phase) {
                    case PLAY -> register(registrar::playToClient, id, codec, receiver);
                    case CONFIGURATION -> register(registrar::configurationToClient, id, codec, receiver);
                    default -> throw new IllegalStateException("Unable to handle the given phase for the following packet: [Id: " + id + ", Phase: " + phase + "]");
                }
            }
            case BI -> {
                switch (phase) {
                    case PLAY -> register(registrar::playBidirectional, id, codec, receiver);
                    case CONFIGURATION -> register(registrar::configurationBidirectional, id, codec, receiver);
                    default -> throw new IllegalStateException("Unable to handle the given phase for the following packet: [Id: " + id + ", Phase: " + phase + "]");
                }
            }
        }
    }

    private static <T extends CustomPayload> void register(TriConsumer<CustomPayload.Id<T>, PacketCodec<? super PacketByteBuf, T>, IPayloadHandler<T>> consumer, CustomPayload.Id<?> id, PacketCodec<? super PacketByteBuf, ?> codec, NetworkReceiver<?> receiver) {
        consumer.accept((CustomPayload.Id<T>) id, (PacketCodec<? super PacketByteBuf, T>) codec, (arg, ctx) -> {
            ((NetworkReceiver<T>) receiver).onPacket(arg, new NetworkReceiver.Context(ctx.protocol() == NetworkPhase.PLAY ? ctx.player() : null, ctx.listener(), ctx::reply, ctx::enqueueWork));
        });
    }

    //public <T extends CustomPayload> PayloadRegistrar playToClient(CustomPayload.Id<T> type, PacketCodec<? super RegistryByteBuf, T> reader, IPayloadHandler<T> handler) {

    public static <T extends CustomPayload> void registerPayloadType(NetworkDirection direction, NetworkPhase phase, CustomPayload.Id<T> type, Endec<T> reader) {
        if(freezeNetworkRegistration) throw new IllegalStateException("NETWORK REGISTRATION FROZEN");

        if(direction != NetworkDirection.BI) {
            var oppositeDir = direction == NetworkDirection.C2S ? NetworkDirection.S2C : NetworkDirection.C2S;

            var otherReader = (Endec<T>) INSTANCE.payloadCodecs.computeIfAbsent(new NetworkConfiguration(oppositeDir, phase), direction1 -> new HashMap<>())
                    .remove(type);

            if(otherReader != null) {
                INSTANCE.payloadCodecs.computeIfAbsent(new NetworkConfiguration(NetworkDirection.BI, phase), direction1 -> new HashMap<>())
                        .put(type, biDirectionalEndec(type,
                                (direction == NetworkDirection.C2S ? otherReader : reader),
                                (direction == NetworkDirection.C2S ? reader : otherReader)
                        ));

                LOGGER.warn("Bi-Directional Packet FOUND! [Id: {}, SameCodec?: {}]", type, reader.equals(otherReader));

                return;
            }
        }

        var map = INSTANCE.payloadCodecs.computeIfAbsent(new NetworkConfiguration(direction, phase), direction1 -> new HashMap<>());

        if(map.containsKey(type)) {
            throw new IllegalStateException("Unable to register the given payload type as it already exists within the map! [Id: " + type + "]");
        }

        map.put(type, reader);
    }

    public static final SerializationAttribute.WithValue<ConnectionSideAttribute> CONNECTION_SIDE_ATTRIBUTE = SerializationAttribute.withValue("connection_side");

    public record ConnectionSideAttribute(boolean isClient) {}

    private static <T extends CustomPayload> Endec<T> biDirectionalEndec(CustomPayload.Id<T> type, Endec<T> clientEndec, Endec<T> serverEndec) {
        return Endec.of((ctx, serializer, value) -> {
            try {
                if (ctx.requireAttributeValue(CONNECTION_SIDE_ATTRIBUTE).isClient()) {
                    clientEndec.encode(ctx, serializer, value);
                } else {
                    serverEndec.encode(ctx, serializer, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("BI_DI ENCODING PACKET BROKEN! [Packet Id: " + type.id() + "]", e);
            }
        }, (ctx, serializer) -> {
            try {
                return (ctx.requireAttributeValue(CONNECTION_SIDE_ATTRIBUTE).isClient())
                        ? clientEndec.decode(ctx, serializer)
                        : serverEndec.decode(ctx, serializer);
            } catch (Exception e) {
                throw new RuntimeException("BI_DI DECODING PACKET BROKEN! [Packet Id: " + type.id() + "]", e);
            }
        });
    }

    private static <T> PacketCodec<? super PacketByteBuf, T> toCodec(Endec<T> endec) {
        return PacketCodec.of(
                (value, buf) -> {
                    var ctx = buf instanceof RegistryByteBuf registryByteBuf
                            ? SerializationContext.attributes(RegistriesAttribute.of(registryByteBuf.getRegistryManager()))
                            : SerializationContext.empty();

                    var server = Owo.currentServer();

                    boolean isOnServer = server != null && buf instanceof RegistryByteBuf registryByteBuf && registryByteBuf.getRegistryManager().equals(server.getRegistryManager());

                    ctx = ctx.withAttributes(CONNECTION_SIDE_ATTRIBUTE.instance(new ConnectionSideAttribute(!isOnServer)));

                    endec.encode(ctx, ByteBufSerializer.of(buf), value);
                },
                buf -> {
                    var ctx = buf instanceof RegistryByteBuf registryByteBuf
                            ? SerializationContext.attributes(RegistriesAttribute.of(registryByteBuf.getRegistryManager()))
                            : SerializationContext.empty();

                    var server = Owo.currentServer();

                    boolean isOnServer = server != null && buf instanceof RegistryByteBuf registryByteBuf && registryByteBuf.getRegistryManager().equals(server.getRegistryManager());

                    ctx = ctx.withAttributes(CONNECTION_SIDE_ATTRIBUTE.instance(new ConnectionSideAttribute(!isOnServer)));

                    return endec.decode(ctx, ByteBufDeserializer.of(buf));
                }
        );
    }

    public static <T extends CustomPayload> void registerReceiver(NetworkDirection direction, NetworkPhase phase, CustomPayload.Id<T> type, NetworkReceiver<T> receiver) {
        registerReceiverAsync(direction, phase, type, receiver.async());
    }

    public static <T extends CustomPayload> void registerReceiverAsync(NetworkDirection direction, NetworkPhase phase, CustomPayload.Id<T> type, NetworkReceiver<T> receiver) {
        if(freezeNetworkRegistration) throw new IllegalStateException("NETWORK REGISTRATION FROZEN");

        if(direction != NetworkDirection.BI) {
            var oppositeDir = direction == NetworkDirection.C2S ? NetworkDirection.S2C : NetworkDirection.C2S;

            var otherReceiver = (NetworkReceiver<T>) INSTANCE.receivers.computeIfAbsent(new NetworkConfiguration(oppositeDir, phase), direction1 -> new HashMap<>())
                    .remove(type);

            if(otherReceiver != null) {
                INSTANCE.receivers.computeIfAbsent(new NetworkConfiguration(NetworkDirection.BI, phase), direction1 -> new HashMap<>())
                        .put(type, NetworkReceiver.sidedReceiver(
                                (direction == NetworkDirection.C2S ? otherReceiver : receiver),
                                (direction == NetworkDirection.C2S ? receiver : otherReceiver)
                        ));

                return;
            }
        }

        var map = INSTANCE.receivers.computeIfAbsent(new NetworkConfiguration(direction, phase), direction1 -> new HashMap<>());

        if(map.containsKey(type)) {
            throw new IllegalStateException("Unable to register the given payload type as it already exists within the map! [Id: " + type + "]");
        }

        map.put(type, receiver);
    }

    private record NetworkConfiguration(NetworkDirection direction, NetworkPhase phase) { }
}
