package io.wispforest.owo.extras.network;

import com.mojang.logging.LogUtils;
import io.wispforest.owo.Owo;
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

    private final Map<NetworkConfiguration, Map<CustomPayload.Id<?>, PacketCodec<? super PacketByteBuf, ?>>> payloadCodecs = new HashMap<>();
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
                var codec = payloadCodecs.remove(id);

                if(codec == null) {
                    throw new IllegalStateException("Unable to get the required codec to serialize the given packet: [Id: " + id + "]");
                }

                registerForConfiguration(configuration, id, codec, receiver);
            });
        });

        this.payloadCodecs.forEach((configuration, packetCodecs) -> {
            var payloadCodecs = this.payloadCodecs.getOrDefault(configuration, Map.of());

            payloadCodecs.forEach((id, codec) -> {
                registerForConfiguration(configuration, id, codec, (packet, context) -> {});
            });
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
            ctx.enqueueWork(() -> ((NetworkReceiver<T>) receiver).onPacket(arg, new NetworkReceiver.Context(ctx.protocol() == NetworkPhase.PLAY ? ctx.player() : null, ctx.listener(), ctx::reply)));
        });
    }

    //public <T extends CustomPayload> PayloadRegistrar playToClient(CustomPayload.Id<T> type, PacketCodec<? super RegistryByteBuf, T> reader, IPayloadHandler<T> handler) {

    public static <T extends CustomPayload> void registerPayloadType(NetworkDirection direction, NetworkPhase phase, CustomPayload.Id<T> type, PacketCodec<? super PacketByteBuf, T> reader) {
        if(freezeNetworkRegistration) throw new IllegalStateException("NETWORK REGISTRATION FROZEN");

        if(direction != NetworkDirection.BI) {
            var oppositeDir = direction == NetworkDirection.C2S ? NetworkDirection.S2C : NetworkDirection.C2S;

            var otherReader = (PacketCodec<? super PacketByteBuf, T>) INSTANCE.payloadCodecs.computeIfAbsent(new NetworkConfiguration(oppositeDir, phase), direction1 -> new HashMap<>())
                    .remove(type);

            if(otherReader != null) {
                INSTANCE.payloadCodecs.computeIfAbsent(new NetworkConfiguration(NetworkDirection.BI, phase), direction1 -> new HashMap<>())
                        .put(type, biDirectionalCodec(
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

    private static <T> PacketCodec<? super PacketByteBuf, T> biDirectionalCodec(PacketCodec<? super PacketByteBuf, T> clientCodec, PacketCodec<? super PacketByteBuf, T> serverCodec) {
        return PacketCodec.of(
                (value, buf) -> {
                    var server = Owo.currentServer();

                    if(server != null && buf instanceof RegistryByteBuf registryByteBuf && registryByteBuf.getRegistryManager().equals(server.getRegistryManager())) {
                        serverCodec.encode(buf, value);

                        return;
                    }

                    clientCodec.encode(buf, value);
                },
                buf -> {
                    var server = Owo.currentServer();

                    if(server != null && buf instanceof RegistryByteBuf registryByteBuf && registryByteBuf.getRegistryManager().equals(server.getRegistryManager())) {
                        return serverCodec.decode(buf);
                    }

                    return clientCodec.decode(buf);
                }
        );
    }

    public static <T extends CustomPayload> void registerReceiver(NetworkDirection direction, NetworkPhase phase, CustomPayload.Id<T> type, NetworkReceiver<T> receiver) {
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
