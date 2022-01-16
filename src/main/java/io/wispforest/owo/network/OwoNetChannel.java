package io.wispforest.owo.network;

import io.wispforest.owo.network.serialization.RecordSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class OwoNetChannel {

    private final Map<Class<?>, IndexedSerializer<?>> serializersByClass = new HashMap<>();
    private final Int2ObjectMap<IndexedSerializer<?>> serializersByIndex = new Int2ObjectOpenHashMap<>();

    @SuppressWarnings("rawtypes") private final List<ClientChannelHandler> clientHandlers = new ArrayList<>();
    @SuppressWarnings("rawtypes") private final List<ServerChannelHandler> serverHandlers = new ArrayList<>();

    private final Identifier packetId;

    private ClientHandle clientHandle = null;
    private ServerHandle serverHandle = null;

    @SuppressWarnings("unchecked")
    public OwoNetChannel(Identifier id) {
        this.packetId = id;

        ServerPlayNetworking.registerGlobalReceiver(packetId, (server, player, handler, buf, responseSender) -> {
            int handlerIndex = buf.readVarInt();
            final Record message = serializersByIndex.get(handlerIndex).serializer.read(buf);
            server.execute(() -> serverHandlers.get(handlerIndex).handle(message, server, player, handler));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(packetId, (client, handler, buf, responseSender) -> {
                int handlerIndex = buf.readVarInt();
                final Record message = serializersByIndex.get(-handlerIndex).serializer.read(buf);
                client.execute(() -> clientHandlers.get(handlerIndex).handle(message, client, handler));
            });
        }
    }

    public <R extends Record> void registerClientbound(Class<R> messageClass, ClientChannelHandler<R> handler) {
        int index = clientHandlers.size();
        this.createSerializer(messageClass, index, EnvType.CLIENT);
        this.clientHandlers.add(handler);
    }

    public <R extends Record> void registerServerbound(Class<R> messageClass, ServerChannelHandler<R> handler) {
        int index = serverHandlers.size();
        this.createSerializer(messageClass, index, EnvType.SERVER);
        this.serverHandlers.add(handler);
    }

    public ClientHandle clientHandle() {
        if (this.clientHandle == null) this.clientHandle = new ClientHandle();
        return clientHandle;
    }

    public ServerHandle serverHandle(MinecraftServer server) {
        var handle = getServerHandle();
        handle.targets = PlayerLookup.all(server);
        return handle;
    }

    public ServerHandle serverHandle(Collection<ServerPlayerEntity> targets) {
        var handle = getServerHandle();
        handle.targets = targets;
        return handle;
    }

    public ServerHandle serverHandle(ServerPlayerEntity player) {
        var handle = getServerHandle();
        handle.targets = Collections.singleton(player);
        return handle;
    }

    private ServerHandle getServerHandle() {
        if (this.serverHandle == null) this.serverHandle = new ServerHandle();
        return serverHandle;
    }

    private <R extends Record> void createSerializer(Class<R> messageClass, int handlerIndex, EnvType target) {
        var serializer = serializersByClass.get(messageClass);
        if (serializer == null) {
            final var indexedSerializer = IndexedSerializer.create(RecordSerializer.create(messageClass), handlerIndex, target);
            serializersByClass.put(messageClass, indexedSerializer);
            serializersByIndex.put(target == EnvType.CLIENT ? -handlerIndex : handlerIndex, indexedSerializer);
        } else if (serializer.handlerIndex(target) == -1) {
            serializer.setHandlerIndex(handlerIndex, target);
        } else {
            throw new IllegalStateException("Message class '" + messageClass.getName() + "' is already registered for target environment " + target);
        }
    }

    @SuppressWarnings("unchecked")
    private <R extends Record> PacketByteBuf encode(R message, EnvType target) {
        var buffer = PacketByteBufs.create();

        final var messageClass = message.getClass();

        if (!this.serializersByClass.containsKey(messageClass)) {
            throw new IllegalStateException("Message class '" + messageClass + "' is not registered");
        }

        final IndexedSerializer<R> serializer = (IndexedSerializer<R>) this.serializersByClass.get(messageClass);
        if (serializer.handlerIndex(target) == -1) {
            throw new IllegalStateException("Message class '" + messageClass + "' has not handler registered for target environment " + target);
        }

        buffer.writeVarInt(serializer.handlerIndex(target));
        serializer.serializer.write(buffer, message);

        return buffer;
    }

    public class ClientHandle {
        public <R extends Record> void send(R message) {
            ClientPlayNetworking.send(OwoNetChannel.this.packetId, OwoNetChannel.this.encode(message, EnvType.SERVER));
        }
    }

    public class ServerHandle {

        private Collection<ServerPlayerEntity> targets = Collections.emptySet();

        public <R extends Record> void send(R message) {
            this.targets.forEach(player -> ServerPlayNetworking.send(player, OwoNetChannel.this.packetId, OwoNetChannel.this.encode(message, EnvType.CLIENT)));
            this.targets = null;
        }
    }

    @FunctionalInterface
    public interface ServerChannelHandler<R extends Record> {
        void handle(R message, MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler);
    }

    @FunctionalInterface
    public interface ClientChannelHandler<R extends Record> {
        void handle(R message, MinecraftClient client, ClientPlayNetworkHandler handler);
    }

    private static final class IndexedSerializer<R extends Record> {
        private int clientHandlerIndex = -1;
        private int serverHandlerIndex = -1;

        private final RecordSerializer<R> serializer;

        private IndexedSerializer(RecordSerializer<R> serializer) {
            this.serializer = serializer;
        }

        public static <R extends Record> IndexedSerializer<R> create(RecordSerializer<R> serializer, int index, EnvType target) {
            return new IndexedSerializer<>(serializer).setHandlerIndex(index, target);
        }

        public IndexedSerializer<R> setHandlerIndex(int index, EnvType target) {
            switch (target) {
                case CLIENT -> this.clientHandlerIndex = index;
                case SERVER -> this.serverHandlerIndex = index;
            }
            return this;
        }

        public int handlerIndex(EnvType target) {
            return switch (target) {
                case CLIENT -> clientHandlerIndex;
                case SERVER -> serverHandlerIndex;
            };
        }
    }
}

