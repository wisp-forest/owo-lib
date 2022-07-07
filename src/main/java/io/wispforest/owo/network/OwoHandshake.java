package io.wispforest.owo.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public class OwoHandshake {

    @SuppressWarnings("unchecked")
    private static final PacketBufSerializer<Map<Identifier, Integer>> RESPONSE_SERIALIZER =
            (PacketBufSerializer<Map<Identifier, Integer>>) (Object) PacketBufSerializer.createMapSerializer(Map.class, Identifier.class, Integer.class);

    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§chandshake failure\n"));
    public static final Identifier CHANNEL_ID = new Identifier("owo", "handshake");
    public static final Identifier OFF_CHANNEL_ID = new Identifier("owo", "handshake_off");

    private static final boolean ENABLED = !Boolean.getBoolean("owo.handshake.disable");
    private static boolean HANDSHAKE_REQUIRED = false;
    private static boolean QUERY_RECEIVED = false;

    // ------------
    // Registration
    // ------------

    public static void enable() {}

    public static void requireHandshake() {
        HANDSHAKE_REQUIRED = true;
    }

    static {
        ServerLoginConnectionEvents.QUERY_START.register(OwoHandshake::queryStart);
        ServerLoginNetworking.registerGlobalReceiver(OwoHandshake.CHANNEL_ID, OwoHandshake::syncServer);

        if (!ENABLED) {
            ServerPlayNetworking.registerGlobalReceiver(OwoHandshake.OFF_CHANNEL_ID, (server, player, handler, buf, responseSender) -> {});
        } else {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> QUERY_RECEIVED = true);
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientLoginNetworking.registerGlobalReceiver(OwoHandshake.CHANNEL_ID, OwoHandshake::syncClient);
            ClientPlayConnectionEvents.JOIN.register(OwoHandshake::handleJoinClient);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> QUERY_RECEIVED = false);
            ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> QUERY_RECEIVED = false);
        }
    }

    public static boolean isValid() {
        return ENABLED && QUERY_RECEIVED;
    }

    // -------
    // Packets
    // -------

    private static void queryStart(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        if (!ENABLED) return;

        var request = PacketByteBufs.create();
        writeHashes(request, OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);
        sender.sendPacket(OwoHandshake.CHANNEL_ID, request);
        Owo.LOGGER.info("[Handshake] Sending channel query");
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> syncClient(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        Owo.LOGGER.info("[Handshake] Sending client channels");
        QUERY_RECEIVED = true;

        if (buf.readableBytes() > 0) {
            final var serverOptionalChannels = RESPONSE_SERIALIZER.deserializer().apply(buf);
            ((OwoClientConnectionExtension) clientLoginNetworkHandler.getConnection()).owo$setChannelSet(verifyOptionalServices(serverOptionalChannels, OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel));
        }

        var response = PacketByteBufs.create();
        writeHashes(response, OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel);
        writeHashes(response, ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController);
        writeHashes(response, OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel);

        return CompletableFuture.completedFuture(response);
    }

    private static void syncServer(MinecraftServer server, ServerLoginNetworkHandler handler, boolean responded, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        Owo.LOGGER.info("[Handshake] Receiving client channels");
        if (!responded) {
            if (!HANDSHAKE_REQUIRED) return;

            handler.disconnect(TextOps.concat(PREFIX, Text.of("incompatible client")));
            Owo.LOGGER.info("[Handshake] Handshake failed, client did not respond to channel query");
            return;
        }

        final var clientChannels = RESPONSE_SERIALIZER.deserializer().apply(buf);
        final var clientParticleControllers = RESPONSE_SERIALIZER.deserializer().apply(buf);

        StringBuilder disconnectMessage = new StringBuilder();

        boolean isAllGood = verifyReceivedHashes("channels", clientChannels, OwoNetChannel.REQUIRED_CHANNELS, OwoHandshake::hashChannel, disconnectMessage);
        isAllGood &= verifyReceivedHashes("controllers", clientParticleControllers, ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController, disconnectMessage);

        if (!isAllGood) {
            handler.disconnect(TextOps.concat(PREFIX, Text.of(disconnectMessage.toString())));
        }

        if (buf.readableBytes() > 0) {
            final var clientOptionalChannels = RESPONSE_SERIALIZER.deserializer().apply(buf);
            ((OwoClientConnectionExtension) handler.getConnection()).owo$setChannelSet(verifyOptionalServices(clientOptionalChannels, OwoNetChannel.OPTIONAL_CHANNELS, OwoHandshake::hashChannel));
        }

        Owo.LOGGER.info("[Handshake] Handshake completed successfully");
    }

    @Environment(EnvType.CLIENT)
    private static void handleJoinClient(ClientPlayNetworkHandler handler, PacketSender packetSender, MinecraftClient client) {
        if (QUERY_RECEIVED || ClientPlayNetworking.canSend(OFF_CHANNEL_ID) || !HANDSHAKE_REQUIRED || !ENABLED) return;
        client.execute(() -> {
            handler.getConnection().disconnect(TextOps.concat(PREFIX, Text.of("incompatible server")));
        });
    }

    // -------
    // Utility
    // -------

    private static <T> Set<Identifier> verifyOptionalServices(Map<Identifier, Integer> remoteMap, Map<Identifier, T> localMap, ToIntFunction<T> hashFunction) {
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
                if (!hasMismatchedHashes) disconnectMessage.append(serviceNamePlural).append(" with mismatched hashes:\n");

                disconnectMessage.append("§7").append(entry.getKey()).append("§r\n");

                isAllGood = false;
                hasMismatchedHashes = true;
            }
        }

        return isAllGood;
    }

    private static <T> void writeHashes(PacketByteBuf buffer, Map<Identifier, T> values, ToIntFunction<T> hashFunction) {
        Map<Identifier, Integer> hashes = new HashMap<>();

        for (var entry : values.entrySet()) {
            hashes.put(entry.getKey(), hashFunction.applyAsInt(entry.getValue()));
        }

        RESPONSE_SERIALIZER.serializer().accept(buffer, hashes);
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
        for (var entry : channel.serializersByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey() * 31 + entry.getValue().serializer.getRecordClass().getName().hashCode();
        }
        return 31 * channel.packetId.hashCode() + serializersHash;
    }

    private static int hashController(ParticleSystemController controller) {
        int serializersHash = 0;
        for (var entry : controller.systemsByIndex.int2ObjectEntrySet()) {
            serializersHash += entry.getIntKey();
        }
        return 31 * controller.channelId.hashCode() + serializersHash;
    }
}
