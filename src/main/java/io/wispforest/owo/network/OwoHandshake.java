package io.wispforest.owo.network;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ClientCommonPacketListenerImplAccessor;
import io.wispforest.owo.mixin.ServerCommonPacketListenerImplAccessor;
import io.wispforest.owo.network.neoforge.SidedPacketCodec;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.ServicesFrozenException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public final class OwoHandshake {

    private static final Endec<Map<Identifier, Integer>> CHANNEL_HASHES_ENDEC = Endec.map(MinecraftEndecs.IDENTIFIER, Endec.INT);

    public static final MutableComponent PREFIX = TextOps.concat(Owo.PREFIX, Component.nullToEmpty("§chandshake failure\n"));
    public static final Identifier CHANNEL_ID = Owo.id("handshake");
    public static final Identifier OFF_CHANNEL_ID = Owo.id("handshake_off");

    public static final boolean ENABLED = System.getProperty("owo.handshake.enabled") != null ? Boolean.getBoolean("owo.handshake.enabled") : Owo.DEBUG;
    private static boolean HANDSHAKE_REQUIRED = false;
    private static boolean QUERY_RECEIVED = false;

    private OwoHandshake() {}

    // ------------
    // Registration
    // ------------

    public static void enable() {
        if (OwoFreezer.isFrozen()) {
            throw new ServicesFrozenException("The oωo handshake may only be enabled during mod initialization");
        }
    }

    public static void requireHandshake() {
        if (OwoFreezer.isFrozen()) {
            throw new ServicesFrozenException("The oωo handshake may only be made required during mod initialization");
        }

        HANDSHAKE_REQUIRED = true;
    }

    public static void register(PayloadRegistrar registrar) {
        IPayloadHandler<CustomPayload> handler = (payload, context) -> {
            if (payload instanceof HandshakeRequest request) {
                OwoHandshake.syncClient(request, context);
            } else if (payload instanceof HandshakeResponse response) {
                OwoHandshake.syncServer(response, context);
            } else {
                throw new IllegalStateException("OWO_NEO: HOW DID YOU GET HERE!");
            }
        };

        registrar.configurationBidirectional(
                new CustomPayload.Id<CustomPayload>(OwoHandshake.CHANNEL_ID),
                new SidedPacketCodec<CustomPayload>(
                        CodecUtils.toPacketCodec(HandshakeResponse.ENDEC.xmap(response -> response, customPayload -> (HandshakeResponse) customPayload)),
                        CodecUtils.toPacketCodec(HandshakeRequest.ENDEC.xmap(request -> request, customPayload -> (HandshakeRequest) customPayload))
                ),
                handler,
                handler
        );

        Owo.getModBus().addListener((RegisterConfigurationTasksEvent event) -> {
            var listener = (ServerConfigurationNetworkHandler) event.getListener();
            OwoHandshake.configureStart(listener, ((ServerCommonNetworkHandlerAccessor) listener).owo$server());
        });

        if (!ENABLED) {
            registrar.configurationToClient(HandshakeOff.ID, StreamCodec.unit(new HandshakeOff()), (payload, context) -> {});
        }
    }

    public static void onDisconnect() {
        QUERY_RECEIVED = false;
        QueuedChannelSet.channels = null;
    }

    public static boolean isValidClient() {
        return ENABLED && QUERY_RECEIVED;
    }

    // -------
    // Packets
    // -------

    private static void configureStart(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {
        if (!ENABLED) return;

        if (NetworkRegistry.hasChannel(handler, OFF_CHANNEL_ID)) {
            Owo.LOGGER.info("[Handshake] Handshake disabled by client, skipping");
            return;
        }

        if (!NetworkRegistry.hasChannel(handler, CHANNEL_ID)) {
            if (!HANDSHAKE_REQUIRED) return;

            handler.disconnect(TextOps.concat(PREFIX, Component.nullToEmpty("incompatible client")));
            Owo.LOGGER.info("[Handshake] Handshake failed, client doesn't understand channel packet");
            return;
        }

        var optionalChannels = formatHashes(OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);
        handler.send(new HandshakeRequest(optionalChannels));
        Owo.LOGGER.info("[Handshake] Sending channel packet");
    }

    //@OnlyIn(Dist.CLIENT)
    private static void syncClient(HandshakeRequest request, IPayloadContext context) {
        Owo.LOGGER.info("[Handshake] Sending client channels");
        QUERY_RECEIVED = true;

        QueuedChannelSet.channels = filterOptionalServices(request.optionalChannels(), OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel);

        var requiredChannels = formatHashes(OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel);
        var requiredControllers = formatHashes(ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController);
        var optionalChannels = formatHashes(OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);

        context.reply(new HandshakeResponse(requiredChannels, requiredControllers, optionalChannels));
    }

    private static void syncServer(HandshakeResponse response, IPayloadContext context) {
        Owo.LOGGER.info("[Handshake] Receiving client channels");

        StringBuilder disconnectMessage = new StringBuilder();

        boolean isAllGood = verifyReceivedHashes("channels", response.requiredChannels(), OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel, disconnectMessage);
        isAllGood &= verifyReceivedHashes("controllers", response.requiredControllers(), ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController, disconnectMessage);

        if (!isAllGood) {
            context.disconnect(TextOps.concat(PREFIX, Component.nullToEmpty(disconnectMessage.toString())));
        }

        ((OwoClientConnectionExtension) ((ServerCommonPacketListenerImplAccessor) ((ServerPayloadContext)context).listener()).owo$getConnection()).owo$setChannelSet(filterOptionalServices(response.optionalChannels(), OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel));

        Owo.LOGGER.info("[Handshake] Handshake completed successfully");
    }

//    @OnlyIn(Dist.CLIENT)
//    public static void handleReadyClient(ClientConfigurationPacketListenerImpl handler, Minecraft client) {
//        // TODO: Report issues with ClientConfigurationNetworking.canSend(CHANNEL_ID)
//        if (NetworkRegistry.hasChannel(handler, CHANNEL_ID) || !HANDSHAKE_REQUIRED || !ENABLED) return;
//
//        client.execute(() -> {
//            ((ClientCommonPacketListenerImplAccessor) handler)
//                    .getConnection()
//                    .disconnect(TextOps.concat(PREFIX, Component.nullToEmpty("incompatible server")));
//        });
//    }

    // -------
    // Utility
    // -------

    private static <T> Set<Identifier> filterOptionalServices(Map<Identifier, Integer> remoteMap, Map<Identifier, T> localMap, ToIntFunction<T> hashFunction) {
        Set<Identifier> readableServices = new HashSet<>();

        for (var entry : remoteMap.entrySet()) {
            var service = localMap.get(entry.getKey());

            if (service == null) continue;
            if (hashFunction.applyAsInt(service) != entry.getValue()) continue;

            readableServices.add(entry.getKey());
        }

        return readableServices;
    }

    private static <T> boolean verifyReceivedHashes(String serviceNamePlural, Map<Identifier, Integer> clientMap, Map<Identifier, T> serverMap, ToIntFunction<T> hashFunction, StringBuilder disconnectMessage) {
        boolean isAllGood = true;

        if (!clientMap.keySet().equals(serverMap.keySet())) {
            isAllGood = false;

            var leftovers = findCollisions(clientMap.keySet(), serverMap.keySet());

            if (!leftovers.getA().isEmpty()) {
                disconnectMessage.append("server is missing ").append(serviceNamePlural).append(":\n");
                leftovers.getA().forEach(identifier -> disconnectMessage.append("§7").append(identifier).append("§r\n"));
            }

            if (!leftovers.getB().isEmpty()) {
                disconnectMessage.append("client is missing ").append(serviceNamePlural).append(":\n");
                leftovers.getB().forEach(identifier -> disconnectMessage.append("§7").append(identifier).append("§r\n"));
            }
        }

        boolean hasMismatchedHashes = false;
        for (var entry : clientMap.entrySet()) {
            var actualServiceObject = serverMap.get(entry.getKey());
            if (actualServiceObject == null) continue;

            int localHash = hashFunction.applyAsInt(actualServiceObject);

            if (localHash != entry.getValue()) {
                if (!hasMismatchedHashes) {
                    disconnectMessage.append(serviceNamePlural).append(" with mismatched hashes:\n");
                }

                disconnectMessage.append("§7").append(entry.getKey()).append("§r\n");

                isAllGood = false;
                hasMismatchedHashes = true;
            }
        }

        return isAllGood;
    }

    private static <T> Map<Identifier, Integer> formatHashes(Map<Identifier, T> values, ToIntFunction<T> hashFunction) {
        Map<Identifier, Integer> hashes = new HashMap<>();

        for (var entry : values.entrySet()) {
            hashes.put(entry.getKey(), hashFunction.applyAsInt(entry.getValue()));
        }

        return hashes;
    }

    private static Tuple<Set<Identifier>, Set<Identifier>> findCollisions(Set<Identifier> first, Set<Identifier> second) {
        var firstLeftovers = new HashSet<Identifier>();
        var secondLeftovers = new HashSet<Identifier>();

        first.forEach(identifier -> {
            if (!second.contains(identifier)) firstLeftovers.add(identifier);
        });

        second.forEach(identifier -> {
            if (!first.contains(identifier)) secondLeftovers.add(identifier);
        });

        return new Tuple<>(firstLeftovers, secondLeftovers);
    }

    private static int hashChannel(OwoNetChannel channel) {
        int serializersHash = 0;
        for (var entry : channel.endecsByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey() * 31 + entry.getValue().getRecordClass().getName().hashCode();
        }
        return 31 * channel.packetId.id().hashCode() + serializersHash;
    }

    private static int hashController(ParticleSystemController controller) {
        int serializersHash = 0;
        for (var entry : controller.systemsByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey();
        }
        return 31 * controller.channelId.hashCode() + serializersHash;
    }

    public record HandshakeRequest(Map<Identifier, Integer> optionalChannels) implements CustomPacketPayload {

        public static final Type<HandshakeRequest> ID = new Type<>(OwoHandshake.CHANNEL_ID);
        public static final Endec<HandshakeRequest> ENDEC = StructEndecBuilder.of(
                CHANNEL_HASHES_ENDEC.fieldOf("optionalChannels", HandshakeRequest::optionalChannels),
                HandshakeRequest::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record HandshakeOff() implements CustomPacketPayload {
        public static final Type<HandshakeOff> ID = new Type<>(OwoHandshake.OFF_CHANNEL_ID);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

    }

    private record HandshakeResponse(Map<Identifier, Integer> requiredChannels,
                                     Map<Identifier, Integer> requiredControllers,
                                     Map<Identifier, Integer> optionalChannels) implements CustomPacketPayload {

        public static final Type<HandshakeResponse> ID = new Type<>(OwoHandshake.CHANNEL_ID);
        public static final Endec<HandshakeResponse> ENDEC = StructEndecBuilder.of(
                CHANNEL_HASHES_ENDEC.fieldOf("requiredChannels", HandshakeResponse::requiredChannels),
                CHANNEL_HASHES_ENDEC.fieldOf("requiredControllers", HandshakeResponse::requiredControllers),
                CHANNEL_HASHES_ENDEC.fieldOf("optionalChannels", HandshakeResponse::optionalChannels),
                HandshakeResponse::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static boolean handshakeRequired() {
        return HANDSHAKE_REQUIRED;
    }
}
