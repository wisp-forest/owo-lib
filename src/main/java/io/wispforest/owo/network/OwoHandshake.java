package io.wispforest.owo.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApiStatus.Internal
class OwoHandshake {

    @SuppressWarnings("unchecked")
    private static final PacketBufSerializer<Map<Identifier, Integer>> RESPONSE_SERIALIZER = (PacketBufSerializer<Map<Identifier, Integer>>)(Object)PacketBufSerializer.createMapSerializer(Map.class, Identifier.class, Integer.class);
    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§chandshake failure\n"));
    static Identifier CHANNEL_ID = new Identifier("owo", "handshake");

    static void queryStart(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        sender.sendPacket(OwoHandshake.CHANNEL_ID, PacketByteBufs.create());
        Owo.LOGGER.info("[Handshake] Sending channel query");
    }

    @Environment(EnvType.CLIENT)
    public static CompletableFuture<PacketByteBuf> syncClient(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        Owo.LOGGER.info("[Handshake] Sending client channels");

        var response = PacketByteBufs.create();

        Map<Identifier, Integer> channelMap = new HashMap<>();

        for (OwoNetChannel channel : OwoNetChannel.REGISTERED_CHANNELS.values()) {
            channelMap.put(channel.packetId, hashChannel(channel));
        }

        RESPONSE_SERIALIZER.serializer().accept(response, channelMap);

        return CompletableFuture.completedFuture(response);
    }

    public static void syncServer(MinecraftServer server, ServerLoginNetworkHandler handler, boolean responded, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        Owo.LOGGER.info("[Handshake] Receiving client channels");
        if (!responded) {
            handler.disconnect(TextOps.concat(PREFIX, Text.of("incompatible client")));
            Owo.LOGGER.info("[Handshake] Handshake failed, client did not respond to channel query");
            return;
        }

        final var clientChannels = RESPONSE_SERIALIZER.deserializer().apply(buf);
        boolean isAllGood = true;
        StringBuilder lines = new StringBuilder();
        if (!OwoNetChannel.REGISTERED_CHANNELS.keySet().equals(clientChannels.keySet())) {
            isAllGood = false;

            var leftovers = findCollisions(clientChannels.keySet(), OwoNetChannel.REGISTERED_CHANNELS.keySet());

            if (!leftovers.getLeft().isEmpty()) {
                lines.append("server is missing channels:\n");
                leftovers.getLeft().forEach(identifier -> lines.append("§7").append(identifier).append("§r\n"));
            }

            if (!leftovers.getRight().isEmpty()) {
                lines.append("client is missing channels:\n");
                leftovers.getRight().forEach(identifier -> lines.append("§7").append(identifier).append("§r\n"));
            }
        }


        boolean hasMismatchedHashes = false;
        for (var entry : clientChannels.entrySet()) {
            var actualChannel = OwoNetChannel.REGISTERED_CHANNELS.get(entry.getKey());

            if (actualChannel == null) continue;

            int localHash = hashChannel(actualChannel);

            if (localHash != entry.getValue()) {
                if (!hasMismatchedHashes) lines.append("channels have mismatched hashes:\n");

                lines.append("§7").append(entry.getKey()).append("§r\n");

                isAllGood = false;
                hasMismatchedHashes = true;
            }
        }

        if (!isAllGood) {
            handler.disconnect(TextOps.concat(PREFIX, Text.of(lines.toString())));
        }
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
}
