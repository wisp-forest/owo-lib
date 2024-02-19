package io.wispforest.owo.network;

import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ClientCommonNetworkHandlerAccessor;
import io.wispforest.owo.mixin.ServerCommonNetworkHandlerAccessor;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.ServicesFrozenException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public final class OwoHandshake {

    private static final Endec<Map<Identifier, Integer>> CHANNEL_HASHES_ENDEC = Endec.map(BuiltInEndecs.IDENTIFIER, Endec.INT);

    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§chandshake failure\n"));
    public static final Identifier CHANNEL_ID = new Identifier("owo", "handshake");
    public static final Identifier OFF_CHANNEL_ID = new Identifier("owo", "handshake_off");

    private static final boolean ENABLED = System.getProperty("owo.handshake.enabled") != null ? Boolean.getBoolean("owo.handshake.enabled") : Owo.DEBUG;
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

    static {
        PayloadTypeRegistry.configurationS2C().register(HandshakeRequest.ID, HandshakeRequest.ENDEC.packetCodec());
        PayloadTypeRegistry.configurationC2S().register(HandshakeResponse.ID, HandshakeResponse.ENDEC.packetCodec());

        ServerConfigurationConnectionEvents.CONFIGURE.register(OwoHandshake::configureStart);
        ServerConfigurationNetworking.registerGlobalReceiver(HandshakeResponse.ID, OwoHandshake::syncServer);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            if (!ENABLED) {
                ClientConfigurationNetworking.registerGlobalReceiver(HandshakeOff.ID, (payload, context) -> {});
            }

            ClientConfigurationNetworking.registerGlobalReceiver(HandshakeRequest.ID, OwoHandshake::syncClient);
            ClientConfigurationConnectionEvents.READY.register(OwoHandshake::handleReadyClient);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> QUERY_RECEIVED = false);
            ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> QUERY_RECEIVED = false);
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

        if (ServerConfigurationNetworking.canSend(handler, OFF_CHANNEL_ID)) {
            Owo.LOGGER.info("[Handshake] Handshake disabled by client, skipping");
            return;
        }

        if (!ServerConfigurationNetworking.canSend(handler, CHANNEL_ID)) {
            if (!HANDSHAKE_REQUIRED) return;

            handler.disconnect(TextOps.concat(PREFIX, Text.of("incompatible client")));
            Owo.LOGGER.info("[Handshake] Handshake failed, client doesn't understand channel packet");
            return;
        }

        var optionalChannels = formatHashes(OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);
        ServerConfigurationNetworking.send(handler, new HandshakeRequest(optionalChannels));
        Owo.LOGGER.info("[Handshake] Sending channel packet");
    }

    @Environment(EnvType.CLIENT)
    private static void syncClient(HandshakeRequest request, ClientConfigurationNetworking.Context context) {
        Owo.LOGGER.info("[Handshake] Sending client channels");
        QUERY_RECEIVED = true;

        // TODO: get the actual network handler here.
//        ((OwoClientConnectionExtension) ((ClientCommonNetworkHandlerAccessor) MinecraftClient.getInstance().getNetworkHandler()).getConnection()).owo$setChannelSet(filterOptionalServices(request.optionalChannels(), OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel));

        var requiredChannels = formatHashes(OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel);
        var requiredControllers = formatHashes(ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController);
        var optionalChannels = formatHashes(OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);

        context.responseSender().sendPacket(new HandshakeResponse(requiredChannels, requiredControllers, optionalChannels));
    }

    private static void syncServer(HandshakeResponse response, ServerConfigurationNetworking.Context context) {
        Owo.LOGGER.info("[Handshake] Receiving client channels");

        StringBuilder disconnectMessage = new StringBuilder();

        boolean isAllGood = verifyReceivedHashes("channels", response.requiredChannels(), OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel, disconnectMessage);
        isAllGood &= verifyReceivedHashes("controllers", response.requiredControllers(), ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController, disconnectMessage);

        if (!isAllGood) {
            context.responseSender().disconnect(TextOps.concat(PREFIX, Text.of(disconnectMessage.toString())));
        }

        ((OwoClientConnectionExtension) ((ServerCommonNetworkHandlerAccessor) context.networkHandler()).owo$getConnection()).owo$setChannelSet(filterOptionalServices(response.optionalChannels(), OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel));

        Owo.LOGGER.info("[Handshake] Handshake completed successfully");
    }

    @Environment(EnvType.CLIENT)
    private static void handleReadyClient(ClientConfigurationNetworkHandler handler, MinecraftClient client) {
        if (ClientConfigurationNetworking.canSend(CHANNEL_ID) || !HANDSHAKE_REQUIRED || !ENABLED) return;

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
        return 31 * channel.packetId.id().hashCode() + serializersHash;
    }

    private static int hashController(ParticleSystemController controller) {
        int serializersHash = 0;
        for (var entry : controller.systemsByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey();
        }
        return 31 * controller.channelId.hashCode() + serializersHash;
    }

    public record HandshakeRequest(Map<Identifier, Integer> optionalChannels) implements CustomPayload {
        public static Endec<HandshakeRequest> ENDEC = StructEndecBuilder.of(
            CHANNEL_HASHES_ENDEC.fieldOf("optionalChannels", HandshakeRequest::optionalChannels),
            HandshakeRequest::new
        );
        public static Id<HandshakeRequest> ID = new Id<>(OwoHandshake.CHANNEL_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record HandshakeOff() implements CustomPayload {
        public static Id<HandshakeOff> ID = new Id<>(OwoHandshake.OFF_CHANNEL_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private record HandshakeResponse(Map<Identifier, Integer> requiredChannels,
                                     Map<Identifier, Integer> requiredControllers,
                                     Map<Identifier, Integer> optionalChannels) implements CustomPayload {
        public static Endec<HandshakeResponse> ENDEC = StructEndecBuilder.of(
            CHANNEL_HASHES_ENDEC.fieldOf("requiredChannels", HandshakeResponse::requiredChannels),
            CHANNEL_HASHES_ENDEC.fieldOf("requiredControllers", HandshakeResponse::requiredControllers),
            CHANNEL_HASHES_ENDEC.fieldOf("optionalChannels", HandshakeResponse::optionalChannels),
            HandshakeResponse::new
        );
        public static Id<HandshakeResponse> ID = new Id<>(OwoHandshake.CHANNEL_ID);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
