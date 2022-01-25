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
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public class OwoHandshake {
    private static final boolean DISABLED = Boolean.getBoolean("owo.handshake.disable");
//    private static final boolean DISABLED = true;

    @SuppressWarnings("unchecked")
    private static final PacketBufSerializer<Map<Identifier, Integer>> RESPONSE_SERIALIZER = (PacketBufSerializer<Map<Identifier, Integer>>) (Object) PacketBufSerializer.createMapSerializer(Map.class, Identifier.class, Integer.class);
    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§chandshake failure\n"));

    public static final Identifier CHANNEL_ID = new Identifier("owo", "handshake");
    public static final Identifier OFF_ID = new Identifier("owo", "handshake_off");

    private static final HashSet<ClientConnection> IN_PROGRESS_CONNECTIONS = new HashSet<>();

    private static boolean DID_HANDSHAKE = false;

    // ------------
    // Registration
    // ------------

    public static void enable() {}

    static {
        if (!DISABLED) {
            ServerLoginConnectionEvents.QUERY_START.register(OwoHandshake::queryStart);
            ServerLoginNetworking.registerGlobalReceiver(OwoHandshake.CHANNEL_ID, OwoHandshake::syncServerLogin);

            ServerPlayConnectionEvents.JOIN.register(OwoHandshake::joinStart);
            ServerPlayNetworking.registerGlobalReceiver(OwoHandshake.CHANNEL_ID, OwoHandshake::syncServerPlay);

            ServerLoginConnectionEvents.DISCONNECT.register((handler, server) -> IN_PROGRESS_CONNECTIONS.remove(handler.getConnection()));
            ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> IN_PROGRESS_CONNECTIONS.remove(handler.getConnection()));
            ServerLifecycleEvents.SERVER_STOPPING.register(server -> IN_PROGRESS_CONNECTIONS.clear());
        } else {
            ServerPlayNetworking.registerGlobalReceiver(OwoHandshake.OFF_ID, (server, player, handler, buf, responseSender) -> {});
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientLoginNetworking.registerGlobalReceiver(OwoHandshake.CHANNEL_ID, OwoHandshake::syncClientLogin);

            ClientPlayNetworking.registerGlobalReceiver(OwoHandshake.CHANNEL_ID, OwoHandshake::syncClientPlay);

            ClientPlayConnectionEvents.JOIN.register(OwoHandshake::joinStartClient);

            ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> DID_HANDSHAKE = false);
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> DID_HANDSHAKE = false);
        }
    }

    // -------
    // Packets
    // -------

    private static void queryStart(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        sender.sendPacket(OwoHandshake.CHANNEL_ID, PacketByteBufs.create());
        Owo.LOGGER.info("[LoginHandshake] Sending channel query");
    }

    private static void joinStart(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (!IN_PROGRESS_CONNECTIONS.contains(handler.connection)) return;

        if (!ServerPlayNetworking.canSend(handler, OwoHandshake.CHANNEL_ID)) {
            handler.disconnect(TextOps.concat(PREFIX, Text.of("incompatible client")));
            Owo.LOGGER.info("[PlayHandshake] Play handshake failed, client hasn't defined channel");
            return;
        }

        Owo.LOGGER.info("[PlayHandshake] Sending channel packet");
        sender.sendPacket(OwoHandshake.CHANNEL_ID, PacketByteBufs.create());
    }

    @Environment(EnvType.CLIENT)
    private static void joinStartClient(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (!DID_HANDSHAKE && !ClientPlayNetworking.canSend(OwoHandshake.CHANNEL_ID) && !ClientPlayNetworking.canSend(OwoHandshake.OFF_ID) ) {
            client.execute(() -> {
                handler.getConnection().disconnect(TextOps.concat(PREFIX, Text.of("incompatible server")));
            });
            Owo.LOGGER.info("[PlayHandshake] Play handshake failed, server hasn't defined channel");
        }
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> syncClientLogin(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        DID_HANDSHAKE = true;

        Owo.LOGGER.info("[LoginHandshake] Sending client channels");

        var response = PacketByteBufs.create();
        writeHashes(response, OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel);
        writeHashes(response, ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController);

        return CompletableFuture.completedFuture(response);
    }

    @Environment(EnvType.CLIENT)
    private static void syncClientPlay(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Owo.LOGGER.info("[PlayHandshake] Sending client channels");

        var response = PacketByteBufs.create();
        writeHashes(response, OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel);
        writeHashes(response, ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController);
        sender.sendPacket(OwoHandshake.CHANNEL_ID, response);
    }

    private static void syncServerLogin(MinecraftServer server, ServerLoginNetworkHandler handler, boolean responded, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        Owo.LOGGER.info("[LoginHandshake] Receiving client channels");
        if (!responded) {
            IN_PROGRESS_CONNECTIONS.add(handler.getConnection());
            Owo.LOGGER.info("[LoginHandshake] Login handshake failed, client did not respond to channel query");
            return;
        }

        final var clientChannels = RESPONSE_SERIALIZER.deserializer().apply(buf);
        final var clientParticleControllers = RESPONSE_SERIALIZER.deserializer().apply(buf);

        StringBuilder disconnectMessage = new StringBuilder();

        boolean isAllGood = verifyReceivedHashes("channels", clientChannels, OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel, disconnectMessage);
        isAllGood &= verifyReceivedHashes("controllers", clientParticleControllers, ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController, disconnectMessage);

        if (!isAllGood) {
            handler.disconnect(TextOps.concat(PREFIX, Text.of(disconnectMessage.toString())));
        } else {
            Owo.LOGGER.info("[LoginHandshake] Handshake completed successfully");
        }
    }

    private static void syncServerPlay(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender packetSender) {
        Owo.LOGGER.info("[PlayHandshake] Receiving client channels");

        IN_PROGRESS_CONNECTIONS.remove(handler.getConnection());

        final var clientChannels = RESPONSE_SERIALIZER.deserializer().apply(buf);
        final var clientParticleControllers = RESPONSE_SERIALIZER.deserializer().apply(buf);

        StringBuilder disconnectMessage = new StringBuilder();

        boolean isAllGood = verifyReceivedHashes("channels", clientChannels, OwoNetChannel.REGISTERED_CHANNELS, OwoHandshake::hashChannel, disconnectMessage);
        isAllGood &= verifyReceivedHashes("controllers", clientParticleControllers, ParticleSystemController.REGISTERED_CONTROLLERS, OwoHandshake::hashController, disconnectMessage);

        if (!isAllGood) {
            // For some reason, this doesn't work if you do it normally
            var text = TextOps.concat(PREFIX, Text.of(disconnectMessage.toString()));
            handler.sendPacket(new DisconnectS2CPacket(text));
            server.execute(() -> {
                handler.connection.disconnect(text);
            });
        } else {
            Owo.LOGGER.info("[PlayHandshake] Handshake completed successfully");
        }
    }

    // -------
    // Utility
    // -------

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
