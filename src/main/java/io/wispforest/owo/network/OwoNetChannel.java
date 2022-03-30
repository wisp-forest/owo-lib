package io.wispforest.owo.network;

import io.wispforest.owo.Owo;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.network.serialization.RecordSerializer;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An efficient networking abstraction that uses {@code record}s to store
 * and define packet data. Serialization for most types is fully automatic
 * and no custom handling needs to be done, should one of your record
 * components be of an unsupported type use {@link PacketBufSerializer#register(Class, BiConsumer, Function)}
 * to register a custom serializer.
 *
 * <p> To define a packet class suited for use with this wrapper, simply create a
 * standard Java {@code record} class and put the desired data into the record header.
 *
 * <p>To register a packet onto this channel, use either {@link #registerClientbound(Class, ChannelHandler)}
 * or {@link #registerServerbound(Class, ChannelHandler)}, depending on which direction the packet goes.
 * Bidirectional registration of the same class is explicitly supported. <b>For synchronization purposes,
 * all registration must happen on both client and server, even for clientbound packets. Otherwise,
 * joining the server will fail with a handshake error</b>
 *
 * <p>To send a packet, use any of the {@code handle} methods to obtain a handle for sending. These are
 * named after where the packet is sent <i>from</i>, meaning the {@link #clientHandle()} is used for sending
 * <i>to the server</i> and vice-versa.
 *
 * <p> The registered packet handlers are executed synchronously on the target environment's
 * game thread instead of Netty's event loops - there is no need to call {@code .execute(...)}
 *
 * @see PacketBufSerializer#register(Class, BiConsumer, Function)
 * @see PacketBufSerializer#registerCollectionProvider(Class, Supplier)
 */
public class OwoNetChannel {

    private static boolean FROZEN = false;
    static final Map<Identifier, OwoNetChannel> REGISTERED_CHANNELS = new HashMap<>();
    static final Map<Identifier, OwoNetChannel> REQUIRED_CHANNELS = new HashMap<>();
    static final Map<Identifier, OwoNetChannel> OPTIONAL_CHANNELS = new HashMap<>();

    private final Map<Class<?>, IndexedSerializer<?>> serializersByClass = new HashMap<>();
    final Int2ObjectMap<IndexedSerializer<?>> serializersByIndex = new Int2ObjectOpenHashMap<>();

    private final List<ChannelHandler<Record, ClientAccess>> clientHandlers = new ArrayList<>();
    private final List<ChannelHandler<Record, ServerAccess>> serverHandlers = new ArrayList<>();

    private final Reference2IntMap<Class<?>> deferredClientSerializers = new Reference2IntOpenHashMap<>();

    final Identifier packetId;
    private final String ownerClassName;
    final boolean required;

    private ClientHandle clientHandle = null;
    private ServerHandle serverHandle = null;

    /**
     * Creates a new required channel with given ID. Duplicate channel
     * IDs are not allowed - if there is a collision, the name of the
     * class that previously registered the channel will be part of
     * the exception. <b>This may be called at any stage during
     * mod initialization</b>
     *
     * @param id The desired channel ID
     * @return The created channel
     */
    public static OwoNetChannel create(Identifier id) {
        return new OwoNetChannel(id, ReflectionUtils.getCallingClassName(2), true);
    }

    /**
     * Creates a new optional channel with given ID. Duplicate channel
     * IDs are not allowed - if there is a collision, the name of the
     * class that previously registered the channel will be part of
     * the exception. <b>This may be called at any stage during
     * mod initialization</b>
     *
     * @param id The desired channel ID
     * @return The created channel
     */
    public static OwoNetChannel createOptional(Identifier id) {
        return new OwoNetChannel(id, ReflectionUtils.getCallingClassName(2), false);
    }

    private OwoNetChannel(Identifier id, String ownerClassName, boolean required) {
        if (REGISTERED_CHANNELS.containsKey(id)) {
            throw new IllegalStateException("Channel with id '" + id + "' was already registered from class '" + REGISTERED_CHANNELS.get(id).ownerClassName + "'");
        }

        deferredClientSerializers.defaultReturnValue(-1);

        this.packetId = id;
        this.ownerClassName = ownerClassName;
        this.required = required;

        if (required) {
            OwoHandshake.requireHandshake();
        }

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
        REGISTERED_CHANNELS.put(id, this);

        if (required) {
            REQUIRED_CHANNELS.put(id, this);
        } else {
            OPTIONAL_CHANNELS.put(id, this);
        }
    }

    /**
     * Registers a handler <i>on the client</i> for the specified message class.
     * This also ensures the required serializer is available. If an exception
     * about a missing type adapter is thrown, register one
     *
     * @param messageClass The type of packet data to send and serialize
     * @param handler      The handler that will receive the deserialized
     * @see #serverHandle(PlayerEntity)
     * @see #serverHandle(MinecraftServer)
     * @see #serverHandle(ServerWorld, BlockPos)
     * @see PacketBufSerializer#register(Class, BiConsumer, Function)
     */
    @SuppressWarnings("unchecked")
    public <R extends Record> void registerClientbound(Class<R> messageClass, ChannelHandler<R, ClientAccess> handler) {
        int deferredIndex = deferredClientSerializers.removeInt(messageClass);
        if (deferredIndex != -1) {
            this.clientHandlers.set(deferredIndex, (ChannelHandler<Record, ClientAccess>) handler);
            return;
        }

        int index = this.clientHandlers.size();
        this.createSerializer(messageClass, index, EnvType.CLIENT);
        this.clientHandlers.add((ChannelHandler<Record, ClientAccess>) handler);
    }

    public <R extends Record> void registerClientbound(Class<R> messageClass) {
        int index = this.clientHandlers.size();
        this.createSerializer(messageClass, index, EnvType.CLIENT);
        this.clientHandlers.add(null);

        this.deferredClientSerializers.put(messageClass, index);
    }

    /**
     * Registers a handler <i>on the server</i> for the specified message class.
     * This also ensures the required serializer is available. If an exception
     * about a missing type adapter is thrown, register one
     *
     * @param messageClass The type of packet data to send and serialize
     * @param handler      The handler that will receive the deserialized
     * @see #clientHandle()
     * @see PacketBufSerializer#register(Class, BiConsumer, Function)
     */
    @SuppressWarnings("unchecked")
    public <R extends Record> void registerServerbound(Class<R> messageClass, ChannelHandler<R, ServerAccess> handler) {
        int index = this.serverHandlers.size();
        this.createSerializer(messageClass, index, EnvType.SERVER);
        this.serverHandlers.add((ChannelHandler<Record, ServerAccess>) handler);
    }

    public boolean canSendToPlayer(ServerPlayerEntity player) {
        return canSendToPlayer(player.networkHandler);
    }

    public boolean canSendToPlayer(ServerPlayNetworkHandler networkHandler) {
        if (required) return true;

        return OwoHandshake.isValid() ?
                getChannelSet(networkHandler.connection).contains(this.packetId)
                : ServerPlayNetworking.canSend(networkHandler, this.packetId);
    }

    @Environment(EnvType.CLIENT)
    public boolean canSendToServer() {
        if (required) return true;

        return OwoHandshake.isValid() ?
                getChannelSet(MinecraftClient.getInstance().getNetworkHandler().getConnection()).contains(packetId)
                : ClientPlayNetworking.canSend(this.packetId);
    }

    private static Set<Identifier> getChannelSet(ClientConnection connection) {
        return ((OwoClientConnectionExtension) connection).owo$getChannelSet();
    }

    /**
     * Obtains the client handle of this channel, used to
     * send packets <i>to the server</i>
     *
     * @return The client handle of this channel
     */
    public ClientHandle clientHandle() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
            throw new NetworkException("Cannot obtain client handle in environment type '" + FabricLoader.getInstance().getEnvironmentType() + "'");

        if (this.clientHandle == null) this.clientHandle = new ClientHandle();
        return clientHandle;
    }

    /**
     * Obtains a server handle used to send packets
     * <i>to all players on the given server</i>
     * <p>
     * <b>This handle will be reused - do not retain references</b>
     *
     * @param server The server to target
     * @return A server handle configured for sending packets
     * to all players on the given server
     */
    public ServerHandle serverHandle(MinecraftServer server) {
        var handle = getServerHandle();
        handle.targets = PlayerLookup.all(server);
        return handle;
    }

    /**
     * Obtains a server handle used to send packets
     * <i>to all given players</i>. Use {@link PlayerLookup} to obtain
     * the required collections
     * <p>
     * <b>This handle will be reused - do not retain references</b>
     *
     * @param targets The players to target
     * @return A server handle configured for sending packets
     * to all players in the given collection
     * @see PlayerLookup
     */
    public ServerHandle serverHandle(Collection<ServerPlayerEntity> targets) {
        var handle = getServerHandle();
        handle.targets = targets;
        return handle;
    }

    /**
     * Obtains a server handle used to send packets
     * <i>to the given player only</i>
     * <p>
     * <b>This handle will be reused - do not retain references</b>
     *
     * @param player The player to target
     * @return A server handle configured for sending packets
     * to the given player only
     */
    public ServerHandle serverHandle(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) throw new NetworkException("'player' must be a 'ServerPlayerEntity'");

        var handle = getServerHandle();
        handle.targets = Collections.singleton(serverPlayer);
        return handle;
    }

    /**
     * Obtains a server handle used to send packets
     * <i>to all players tracking the given block entity</i>
     * <p>
     * <b>This handle will be reused - do not retain references</b>
     *
     * @param entity The block entity to look up trackers for
     * @return A server handle configured for sending packets
     * to all players tracking the given block entity
     */
    public ServerHandle serverHandle(BlockEntity entity) {
        if (entity.getWorld().isClient) throw new NetworkException("Server handle cannot be obtained on the client");
        return serverHandle(PlayerLookup.tracking(entity));
    }

    /**
     * Obtains a server handle used to send packets <i>to all
     * players tracking the given position in the given world</i>
     * <p>
     * <b>This handle will be reused - do not retain references</b>
     *
     * @param world The world to look up players in
     * @param pos   The position to look up trackers for
     * @return A server handle configured for sending packets
     * to all players tracking the given position in the given world
     */
    public ServerHandle serverHandle(ServerWorld world, BlockPos pos) {
        return serverHandle(PlayerLookup.tracking(world, pos));
    }

    private ServerHandle getServerHandle() {
        if (this.serverHandle == null) this.serverHandle = new ServerHandle();
        return serverHandle;
    }

    private <R extends Record> void createSerializer(Class<R> messageClass, int handlerIndex, EnvType target) {
        if (FROZEN) {
            throw new NetworkException("Network handlers may only be registered during mod initialization");
        }

        var serializer = serializersByClass.get(messageClass);
        if (serializer == null) {
            final var indexedSerializer = IndexedSerializer.create(RecordSerializer.create(messageClass), handlerIndex, target);
            serializersByClass.put(messageClass, indexedSerializer);
            serializersByIndex.put(target == EnvType.CLIENT ? -handlerIndex : handlerIndex, indexedSerializer);
        } else if (serializer.handlerIndex(target) == -1) {
            serializer.setHandlerIndex(handlerIndex, target);
            serializersByIndex.put(target == EnvType.CLIENT ? -handlerIndex : handlerIndex, serializer);
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

        /**
         * Sends the given message to the server
         *
         * @param message The message to send
         * @see #send(Record[])
         */
        public <R extends Record> void send(R message) {
            ClientPlayNetworking.send(OwoNetChannel.this.packetId, OwoNetChannel.this.encode(message, EnvType.SERVER));
        }

        /**
         * Sends the given messages to the server
         *
         * @param messages The messages to send
         */
        @SafeVarargs
        public final <R extends Record> void send(R... messages) {
            for (R message : messages) send(message);
        }
    }

    public class ServerHandle {

        private Collection<ServerPlayerEntity> targets = Collections.emptySet();

        /**
         * Sends the given message to the configured target(s)
         * <b>Resets the target(s) after sending - this cannot be used
         * for multiple messages on the same handle</b>
         *
         * @param message The message to send
         * @see #send(Record[])
         */
        public <R extends Record> void send(R message) {
            this.targets.forEach(player -> ServerPlayNetworking.send(player, OwoNetChannel.this.packetId, OwoNetChannel.this.encode(message, EnvType.CLIENT)));
            this.targets = null;
        }

        /**
         * Sends the given messages to the configured target(s)
         * <b>Resets the target(s) after sending - this cannot be used
         * multiple times on the same handle</b>
         *
         * @param messages The messages to send
         */
        @SafeVarargs
        public final <R extends Record> void send(R... messages) {
            this.targets.forEach(player -> {
                for (R message : messages) {
                    ServerPlayNetworking.send(player, OwoNetChannel.this.packetId, OwoNetChannel.this.encode(message, EnvType.CLIENT));
                }
            });
            this.targets = null;
        }
    }

    public interface ChannelHandler<R extends Record, E extends EnvironmentAccess<?, ?, ?>> {

        /**
         * Executed on the game thread to handle the incoming
         * message - this can safely modify game state
         *
         * @param message The message that was received
         * @param access  The {@link EnvironmentAccess} used to obtain references
         *                to the execution environment
         */
        void handle(R message, E access);
    }

    /**
     * A simple wrapper that provides access to the environment a packet
     * is being received / message is being handled in
     *
     * @param <P> The type of player to receive the packet
     * @param <R> The runtime that the packet is being received in
     * @param <N> The network handler that received the packet
     */
    public interface EnvironmentAccess<P extends PlayerEntity, R, N> {

        /**
         * @return The player that received the packet
         */
        P player();

        /**
         * @return The environment the packet is being received in,
         * either a {@link MinecraftServer} or a {@link net.minecraft.client.MinecraftClient}
         */
        R runtime();

        /**
         * @return The network handler of the player or client that received the packet,
         * either a {@link net.minecraft.client.network.ClientPlayNetworkHandler} or a
         * {@link net.minecraft.server.network.ServerPlayNetworkHandler}
         */
        N netHandler();
    }

    private void verify() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            if (deferredClientSerializers.size() > 0) {
                throw new NetworkException("Some deferred client handlers for channel " + packetId + " haven't been registered: " + deferredClientSerializers.keySet().stream().map(Class::getName).collect(Collectors.joining(", ")));
            }
        }
    }

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static void freezeAllChannels() {
        FROZEN = true;

        for (OwoNetChannel channel : REGISTERED_CHANNELS.values()) {
            channel.verify();
        }

        if (!Owo.DEBUG) return;
        Owo.LOGGER.info("Channels frozen by '" + ReflectionUtils.getCallingClassName(2) + "'");
    }

    static {
        OwoHandshake.enable();
    }

    static final class IndexedSerializer<R extends Record> {
        private int clientHandlerIndex = -1;
        private int serverHandlerIndex = -1;

        final RecordSerializer<R> serializer;

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

