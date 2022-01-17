package io.wispforest.owo.network;

import io.wispforest.owo.network.serialization.RecordSerializer;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class OwoNetChannel {

    private static final Map<Identifier, String> REGISTERED_CHANNELS = new HashMap<>();

    private final Map<Class<?>, IndexedSerializer<?>> serializersByClass = new HashMap<>();
    private final Int2ObjectMap<IndexedSerializer<?>> serializersByIndex = new Int2ObjectOpenHashMap<>();

    private final List<ChannelHandler<Record, ClientAccess>> clientHandlers = new ArrayList<>();
    private final List<ChannelHandler<Record, ServerAccess>> serverHandlers = new ArrayList<>();

    private final Identifier packetId;

    private ClientHandle clientHandle = null;
    private ServerHandle serverHandle = null;

    public static OwoNetChannel create(Identifier id) {
        return new OwoNetChannel(id, ReflectionUtils.getCallingClassName(2));
    }

    private OwoNetChannel(Identifier id, String ownerClassName) {
        if (REGISTERED_CHANNELS.containsKey(id)) {
            throw new IllegalStateException("Channel with id '" + id + "' was already registered from class '" + REGISTERED_CHANNELS.get(id) + "'");
        }

        this.packetId = id;

        ServerPlayNetworking.registerGlobalReceiver(packetId, (server, player, handler, buf, responseSender) -> {
            int handlerIndex = buf.readVarInt();
            final Record message = serializersByIndex.get(handlerIndex).serializer.read(buf);
            server.execute(() -> serverHandlers.get(handlerIndex).handle(message, new ServerAccess(player)));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(packetId, (client, handler, buf, responseSender) -> {
                int handlerIndex = buf.readVarInt();
                final Record message = serializersByIndex.get(-handlerIndex).serializer.read(buf);
                client.execute(() -> clientHandlers.get(handlerIndex).handle(message, new ClientAccess(handler)));
            });
        }

        clientHandlers.add(null);
        serverHandlers.add(null);
        REGISTERED_CHANNELS.put(id, ownerClassName);
    }

    @SuppressWarnings("unchecked")
    public <R extends Record> void registerClientbound(Class<R> messageClass, ChannelHandler<R, ClientAccess> handler) {
        int index = this.clientHandlers.size();
        this.createSerializer(messageClass, index, EnvType.CLIENT);
        this.clientHandlers.add((ChannelHandler<Record, ClientAccess>) handler);
    }

    @SuppressWarnings("unchecked")
    public <R extends Record> void registerServerbound(Class<R> messageClass, ChannelHandler<R, ServerAccess> handler) {
        int index = this.serverHandlers.size();
        this.createSerializer(messageClass, index, EnvType.SERVER);
        this.serverHandlers.add((ChannelHandler<Record, ServerAccess>) handler);
    }

    public ClientHandle clientHandle() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
            throw new NetworkException("Cannot obtain client handle in environment type '" + FabricLoader.getInstance().getEnvironmentType() + "'");

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

    public ServerHandle serverHandle(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) throw new NetworkException("'player' must be a 'ServerPlayerEntity'");

        var handle = getServerHandle();
        handle.targets = Collections.singleton(serverPlayer);
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
            throw new NetworkException("Message class '" + messageClass + "' is not registered");
        }

        final IndexedSerializer<R> serializer = (IndexedSerializer<R>) this.serializersByClass.get(messageClass);
        if (serializer.handlerIndex(target) == -1) {
            throw new NetworkException("Message class '" + messageClass + "' has not handler registered for target environment " + target);
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

    public interface ChannelHandler<R extends Record, E extends EnvironmentAccess<?, ?, ?>> {
        void handle(R message, E access);
    }

    public interface EnvironmentAccess<P extends PlayerEntity, R, N> {
        P player();

        R runtime();

        N netHandler();
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

