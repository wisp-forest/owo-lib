package io.wispforest.owo.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.annotations.ElementType;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.network.serialization.RecordSerializer;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApiStatus.Internal
class OwoHandshake {

    private static final RecordSerializer<Response> RESPONSE_SERIALIZER = RecordSerializer.create(Response.class);
    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§chandshake failure\n"));
    static Identifier CHANNEL_ID = new Identifier("owo", "handshake");

    @SuppressWarnings("unchecked")
    private static final PacketBufSerializer<Set<Identifier>> IDENTIFIER_SERIALIZER =
            (PacketBufSerializer<Set<Identifier>>) (Object) PacketBufSerializer.createCollectionSerializer(Collection.class, Identifier.class);

    static void queryStart(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        sender.sendPacket(OwoHandshake.CHANNEL_ID, PacketByteBufs.create());
        Owo.LOGGER.info("[Handshake] Sending channel query");
    }

    @Environment(EnvType.CLIENT)
    public static CompletableFuture<PacketByteBuf> syncClient(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        Owo.LOGGER.info("[Handshake] Sending client channels");

        var response = PacketByteBufs.create();
        IDENTIFIER_SERIALIZER.serializer().accept(response, OwoNetChannel.REGISTERED_CHANNELS.keySet());

        RESPONSE_SERIALIZER.write(response,
                new Response(OwoNetChannel.REGISTERED_CHANNELS.values().stream().map(ChannelWrapper::new).toList()));

        return CompletableFuture.completedFuture(response);
    }

    public static void syncServer(MinecraftServer server, ServerLoginNetworkHandler handler, boolean responded, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
        Owo.LOGGER.info("[Handshake] Receiving client channels");
        if (!responded) {
            handler.disconnect(TextOps.concat(PREFIX, Text.of("incompatible client")));
            Owo.LOGGER.info("[Handshake] Handshake failed, client did not respond to channel query");
            return;
        }

        final var clientChannels = IDENTIFIER_SERIALIZER.deserializer().apply(buf);
        if (!OwoNetChannel.REGISTERED_CHANNELS.keySet().equals(clientChannels)) {
            var response = new StringBuilder();
            var leftovers = findCollisions(clientChannels, OwoNetChannel.REGISTERED_CHANNELS.keySet());

            if (!leftovers.getLeft().isEmpty()) {
                response.append("server is missing channels:\n");
                leftovers.getLeft().forEach(identifier -> response.append("§7").append(identifier).append("\n"));
            }

            if (!leftovers.getRight().isEmpty()) {
                response.append("client is missing channels:\n");
                leftovers.getRight().forEach(identifier -> response.append("§7").append(identifier).append("\n"));
            }

            handler.disconnect(TextOps.concat(PREFIX, Text.of(response.toString())));
            return;
        }

        final var response = RESPONSE_SERIALIZER.read(buf);

        for (var channel : response.channels()) {
            var actualChannel = OwoNetChannel.REGISTERED_CHANNELS.get(channel.id());
            for (var serializer : channel.serializers()) {
                if (findSerializer(serializer, actualChannel)) continue;
                handler.disconnect(TextOps.concat(PREFIX, Text.of("serializer mismatch")));
                return;
            }
        }

        Owo.LOGGER.info(response);

        handler.disconnect(TextOps.concat(Owo.PREFIX, Text.of("\n channels matched successfully")));
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

    private static boolean findSerializer(SerializerWrapper wrapper, OwoNetChannel channel) {
        for (var serializer : channel.serializersByClass.values()) {
            if (!className(serializer).equals(wrapper.className)) continue;
            if (serializer.handlerIndex(EnvType.CLIENT) != wrapper.clientIndex) return false;
            return serializer.handlerIndex(EnvType.SERVER) == wrapper.serverIndex;
        }
        return false;
    }

    private static <T> String className(T t) {
        return t.getClass().getName();
    }

    public static record Response(@ElementType(ChannelWrapper.class) Collection<ChannelWrapper> channels) {}

    public static record ChannelWrapper(Identifier id, @ElementType(SerializerWrapper.class) Collection<SerializerWrapper> serializers) {
        public ChannelWrapper(OwoNetChannel channel) {
            this(channel.packetId, channel.serializersByClass.values().stream().map(SerializerWrapper::new).toList());
        }
    }

    public static record SerializerWrapper(int clientIndex, int serverIndex, String className) {
        public SerializerWrapper(OwoNetChannel.IndexedSerializer<?> parent) {
            this(parent.handlerIndex(EnvType.CLIENT), parent.handlerIndex(EnvType.SERVER), parent.serializer.getClass().getName());
        }
    }
}
