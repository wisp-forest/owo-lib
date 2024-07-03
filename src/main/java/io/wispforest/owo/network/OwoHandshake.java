package io.wispforest.owo.network;

import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.extras.ClientConfigurationConnectionEvents;
import io.wispforest.owo.extras.ClientPlayConnectionEvents;
import io.wispforest.owo.extras.ServerConfigurationConnectionEvents;
import io.wispforest.owo.mixin.ClientCommonNetworkHandlerAccessor;
import io.wispforest.owo.mixin.ServerCommonNetworkHandlerAccessor;
import io.wispforest.owo.extras.network.NetworkDirection;
import io.wispforest.owo.extras.network.NetworkReceiver;
import io.wispforest.owo.extras.network.OwoInternalNetworking;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public final class OwoHandshake {

    private static final Endec<Map<Identifier, Integer>> CHANNEL_HASHES_ENDEC = Endec.map(MinecraftEndecs.IDENTIFIER, Endec.INT);

    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§chandshake failure\n"));

    public static final Identifier CHANNEL_ID = Identifier.of("owo", "handshake");

    public static final Identifier CHANNEL_ID_RESPONSE = Identifier.of("owo", "handshake_response");
    public static final Identifier CHANNEL_ID_REQUEST = Identifier.of("owo", "handshake_request");

    public static final Identifier OFF_CHANNEL_ID = Identifier.of("owo", "handshake_off");

    private static final boolean ENABLED = System.getProperty("owo.handshake.enabled") != null ? Boolean.getBoolean("owo.handshake.enabled") : Owo.DEBUG;
    private static boolean HANDSHAKE_REQUIRED = false;
    private static boolean QUERY_RECEIVED = false;

    private OwoHandshake() {}

    // ------------
    // Registration
    // ------------

    public static void enable() {
//        if (OwoFreezer.isFrozen()) {
//            throw new ServicesFrozenException("The oωo handshake may only be enabled during mod initialization");
//        }
    }

    public static void requireHandshake() {
//        if (OwoFreezer.isFrozen()) {
//            throw new ServicesFrozenException("The oωo handshake may only be made required during mod initialization");
//        }

        HANDSHAKE_REQUIRED = true;
    }

    public static void init(IEventBus eventBus) {
        OwoInternalNetworking.registerPayloadType(NetworkDirection.S2C, NetworkPhase.CONFIGURATION, HandshakeRequest.ID, HandshakeRequest.ENDEC);
        OwoInternalNetworking.registerPayloadType(NetworkDirection.C2S, NetworkPhase.CONFIGURATION, HandshakeResponse.ID, HandshakeResponse.ENDEC);

        eventBus.addListener((RegisterConfigurationTasksEvent event) -> {
            event.register(new ICustomConfigurationTask() {
                @Override
                public void run(Consumer<CustomPayload> consumer) {
                    OwoHandshake.configureStart((ServerConfigurationNetworkHandler) event.getListener(), Owo.currentServer());
                }

                @Override
                public Key getKey() {
                    return new Key(CHANNEL_ID);
                }
            });
        });
//        ServerConfigurationConnectionEvents.CONFIGURE.register(OwoHandshake::configureStart);
        OwoInternalNetworking.registerReceiver(NetworkDirection.C2S, NetworkPhase.CONFIGURATION, HandshakeResponse.ID, OwoHandshake::syncServer);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            if (!ENABLED) {
                OwoInternalNetworking.registerPayloadType(NetworkDirection.S2C, NetworkPhase.CONFIGURATION, HandshakeOff.ID, Endec.VOID.xmap(unused -> new HandshakeOff(), o -> null));
                OwoInternalNetworking.registerReceiver(NetworkDirection.C2S, NetworkPhase.CONFIGURATION, HandshakeOff.ID, (payload, context) -> {});
            }

            OwoInternalNetworking.registerReceiver(NetworkDirection.S2C, NetworkPhase.CONFIGURATION, HandshakeRequest.ID, OwoHandshake::syncClient);
            ClientConfigurationConnectionEvents.COMPLETE.register(OwoHandshake::handleReadyClient);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                QUERY_RECEIVED = false;
                QueuedChannelSet.channels = null;
            });

            ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> {
                QUERY_RECEIVED = false;
                QueuedChannelSet.channels = null;
            });
        }
    }

    public static boolean isValidClient() {
        return ENABLED && QUERY_RECEIVED;
    }

    // -------
    // Packets
    // -------

    private static void configureStart(ServerConfigurationNetworkHandler handler, MinecraftServer server) {
        if (!ENABLED) return;

        if (NetworkRegistry.hasChannel(handler, OFF_CHANNEL_ID)) {
            Owo.LOGGER.info("[Handshake] Handshake disabled by client, skipping");
            return;
        }

        if (!NetworkRegistry.hasChannel(handler, CHANNEL_ID_REQUEST)) {
            if (!HANDSHAKE_REQUIRED) return;

            handler.disconnect(TextOps.concat(PREFIX, Text.of("incompatible client")));
            Owo.LOGGER.info("[Handshake] Handshake failed, client doesn't understand channel packet");
            return;
        }

        var optionalChannels = formatHashes(OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);
        handler.send(new HandshakeRequest(optionalChannels));
        Owo.LOGGER.info("[Handshake] Sending channel packet");
    }

    @OnlyIn(Dist.CLIENT)
    private static void syncClient(HandshakeRequest request, NetworkReceiver.Context context) {
        Owo.LOGGER.info("[Handshake] Sending client channels");
        QUERY_RECEIVED = true;

        QueuedChannelSet.channels = filterOptionalServices(request.optionalChannels(), OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel);

        var requiredChannels = formatHashes(OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel);
        var requiredControllers = formatHashes(ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController);
        var optionalChannels = formatHashes(OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);

        context.responseSender().accept(new HandshakeResponse(requiredChannels, requiredControllers, optionalChannels));
    }

    private static void syncServer(HandshakeResponse response, NetworkReceiver.Context context) {
        Owo.LOGGER.info("[Handshake] Receiving client channels");

        StringBuilder disconnectMessage = new StringBuilder();

        boolean isAllGood = verifyReceivedHashes("channels", response.requiredChannels(), OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel, disconnectMessage);
        isAllGood &= verifyReceivedHashes("controllers", response.requiredControllers(), ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController, disconnectMessage);

        if (!isAllGood) {
            context.disconnect(TextOps.concat(PREFIX, Text.of(disconnectMessage.toString())));
        }

        ((OwoClientConnectionExtension) ((ServerCommonNetworkHandlerAccessor) context.listener()).owo$getConnection()).owo$setChannelSet(filterOptionalServices(response.optionalChannels(), OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel));

        Owo.LOGGER.info("[Handshake] Handshake completed successfully");

        ((ServerConfigurationNetworkHandler) context.listener()).onTaskFinished(new ServerPlayerConfigurationTask.Key(CHANNEL_ID));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleReadyClient(ClientConfigurationNetworkHandler handler, MinecraftClient client) {
        if (NetworkRegistry.hasChannel(handler, CHANNEL_ID_RESPONSE) || !HANDSHAKE_REQUIRED || !ENABLED) return;

        client.execute(() -> {
            ((ClientCommonNetworkHandlerAccessor) handler)
                    .getConnection()
                    .disconnect(TextOps.concat(PREFIX, Text.of("incompatible server")));
        });
    }

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

            if (!leftovers.getLeft().isEmpty()) {
                disconnectMessage.append("server is missing ").append(serviceNamePlural).append(":\n");
                leftovers.getLeft().forEach(identifier -> disconnectMessage.append("§7").append(identifier).append("§r\n"));
            }

            if (!leftovers.getRight().isEmpty()) {
                disconnectMessage.append("client is missing ").append(serviceNamePlural).append(":\n");
                leftovers.getRight().forEach(identifier -> disconnectMessage.append("§7").append(identifier).append("§r\n"));
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

    private static Pair<Set<Identifier>, Set<Identifier>> findCollisions(Set<Identifier> first, Set<Identifier> second) {
        var firstLeftovers = new HashSet<Identifier>();
        var secondLeftovers = new HashSet<Identifier>();

        first.forEach(identifier -> {
            if (!second.contains(identifier)) firstLeftovers.add(identifier);
        });

        second.forEach(identifier -> {
            if (!first.contains(identifier)) secondLeftovers.add(identifier);
        });

        return new Pair<>(firstLeftovers, secondLeftovers);
    }

    private static int hashChannel(OwoNetChannel channel) {
        int serializersHash = 0;
        for (var entry : channel.endecsByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey() * 31 + entry.getValue().getRecordClass().getName().hashCode();
        }
        return 31 * channel.channelId.hashCode() + serializersHash;
    }

    private static int hashController(ParticleSystemController controller) {
        int serializersHash = 0;
        for (var entry : controller.systemsByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey();
        }
        return 31 * controller.channelId.hashCode() + serializersHash;
    }

    public record HandshakeRequest(Map<Identifier, Integer> optionalChannels) implements CustomPayload {

        public static final Id<HandshakeRequest> ID = new Id<>(OwoHandshake.CHANNEL_ID_REQUEST);
        public static final Endec<HandshakeRequest> ENDEC = StructEndecBuilder.of(
                CHANNEL_HASHES_ENDEC.fieldOf("optionalChannels", HandshakeRequest::optionalChannels),
                HandshakeRequest::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record HandshakeOff() implements CustomPayload {
        public static final Id<HandshakeOff> ID = new Id<>(OwoHandshake.OFF_CHANNEL_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

    }

    private record HandshakeResponse(Map<Identifier, Integer> requiredChannels,
                                     Map<Identifier, Integer> requiredControllers,
                                     Map<Identifier, Integer> optionalChannels) implements CustomPayload {

        public static final Id<HandshakeResponse> ID = new Id<>(OwoHandshake.CHANNEL_ID_RESPONSE);
        public static final Endec<HandshakeResponse> ENDEC = StructEndecBuilder.of(
                CHANNEL_HASHES_ENDEC.fieldOf("requiredChannels", HandshakeResponse::requiredChannels),
                CHANNEL_HASHES_ENDEC.fieldOf("requiredControllers", HandshakeResponse::requiredControllers),
                CHANNEL_HASHES_ENDEC.fieldOf("optionalChannels", HandshakeResponse::optionalChannels),
                HandshakeResponse::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
