package io.wispforest.owo.config;

import com.google.common.collect.*;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ServerCommonPacketListenerImplAccessor;
import io.wispforest.owo.network.CommonAccess;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.util.Observable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    private static final Map<Connection, Map<String, Map<Option.Key, Observable>>> CLIENT_OPTION_STORAGE = new WeakHashMap<>();
    private static final Event<InitialSync> ON_INITIAL_SYNC = EventFactory.createArrayBacked(InitialSync.class, callbacks -> player -> {
        for (var callback : callbacks) callback.onClientSync(player);
    });

    private static final Map<String, ConfigWrapper<?>> KNOWN_CONFIGS = new HashMap<>();
    private static final MutableComponent PREFIX = TextOps.concat(Owo.PREFIX, Component.nullToEmpty("§cunrecoverable config mismatch\n\n"));

    //--

    public static Event<InitialSync> initialSync() {
        return ON_INITIAL_SYNC;
    }

    interface InitialSync {
        void onClientSync(ServerPlayer player);
    }

    /**
     * @return The state of the client option as a {@link Observable} with the type of {@link T} or null if non-existent
     * @see #getClientOption(ServerPlayer, String, Option.Key)
     */
    public static <T> @Nullable Observable<T> getClientOption(ServerPlayer player, Option<T> option) {
        return (Observable<T>) getClientOption(player, option.configName(), option.key());
    }

    /**
     * @return The state of the client option as a {@link Observable} or null if non-existent
     * @see #getClientOption(ServerPlayer, Option)
     */
    public static @Nullable Observable<?> getClientOption(ServerPlayer player, String configName, Option.Key optionKey) {
        var storage = CLIENT_OPTION_STORAGE.get(((ServerCommonPacketListenerImplAccessor) player.connection).owo$getConnection());
        if (storage == null) return null;
        var map = storage.get(configName);
        return map != null ? map.get(optionKey) : null;
    }

    //--

    /**
     * Retrieve the options which the given player's client
     * sent to the server during config synchronization
     *
     * @param player     The player for which to retrieve the client values
     * @param configName The name of the config for which to retrieve values
     * @return The player's client's values of the given config options,
     * or {@code null} if no config with the given name was synced
     */
    @Deprecated
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayer player, String configName) {
        var storage = CLIENT_OPTION_STORAGE.get(((ServerCommonPacketListenerImplAccessor) player.connection).owo$getConnection());
        if (storage == null) return null;
        var map = storage.get(configName);
        if (map == null) return null;

        return new Map<>() {
            @Override public int size() { return map.size(); }
            @Override public boolean isEmpty() { return map.isEmpty(); }
            @Override public boolean containsKey(Object key) { return map.containsKey(key); }
            @Override public boolean containsValue(Object value) { return false; }
            @Override public Object get(Object key) { return map.get(key).get(); }

            @Override public @org.jspecify.annotations.Nullable Object put(Option.Key key, Object value) { throw new IllegalStateException("can not run 'put' as this is immutable"); }
            @Override public Object remove(Object key) { throw new IllegalStateException("can not run 'put' as this is immutable"); }
            @Override public void putAll(@NonNull Map<? extends Option.Key, ?> m) { throw new IllegalStateException("can not run 'put' as this is immutable"); }
            @Override public void clear() { throw new IllegalStateException("can not run 'put' as this is immutable"); }

            @Override public @NonNull Set<Option.Key> keySet() { return map.keySet(); }

            @Override
            public @NonNull Collection<Object> values() {
                var list = new ArrayList<Object>();
                for (var entry : map.values()) list.add(entry.get());
                return list;
            }

            @Override
            public @NonNull Set<Entry<Option.Key, Object>> entrySet() {
                var set = new HashSet<Entry<Option.Key, Object>>();
                for (var entry : map.entrySet()) set.add(Map.entry(entry.getKey(), entry.getValue().get()));
                return set;
            }
        };
    }

    /**
     * Safer, more clear version of {@link #getClientOptions(ServerPlayer, String)} to
     * be used when the actual config wrapper is available
     *
     * @see #getClientOptions(ServerPlayer, String)
     */
    @Deprecated
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayer player, ConfigWrapper<?> config) {
        return getClientOptions(player, config.name());
    }

    //--

    static void register(ConfigWrapper<?> config, boolean isRequired) {
        KNOWN_CONFIGS.put(config.name(), config);

        if (isRequired) Owo.MAIN.isRequired(true);
    }

    @Environment(EnvType.CLIENT)
    static void syncClientOption(ConfigWrapper<?> config, Option<?> option) {
        var optionBuf = FriendlyByteBufs.create();
        option.write(optionBuf);
        Owo.MAIN.clientHandle().send(new ConfigSyncPacket(config.name(), option.key().asString(), optionBuf));
    }

    private static ConfigsSyncPacket toPacket(Option.SyncMode targetMode) {
        Map<String, ConfigEntry> configs = new HashMap<>();

        KNOWN_CONFIGS.forEach((configName, config) -> {
            var entry = new ConfigEntry(new HashMap<>());

            config.allOptions().forEach((key, option) -> {
                if (option.syncMode().ordinal() < targetMode.ordinal()) return;

                FriendlyByteBuf optionBuf = FriendlyByteBufs.create();
                option.write(optionBuf);

                entry.options().put(key.asString(), optionBuf);
            });

            configs.put(configName, entry);
        });

        return new ConfigsSyncPacket(configs);
    }

    private static @Nullable ConfigWrapper<?> getConfig(String configName) {
        var config = KNOWN_CONFIGS.get(configName);
        if (config == null) Owo.LOGGER.error("Received overrides for unknown config '{}', skipping", configName);
        return config;
    }

    private static void read(ConfigsSyncPacket packet, BiConsumer<Option<?>, FriendlyByteBuf> optionConsumer) {
        for (var configEntry : packet.configs().entrySet()) {
            var config = getConfig(configEntry.getKey());
            if (config == null) continue;
            for (var optionEntry : configEntry.getValue().options().entrySet()) {
                handleEntry(config, optionEntry.getKey(), optionEntry.getValue(), optionConsumer);
            }
        }
    }

    private static void handleEntry(ConfigWrapper<?> config, String entryKey, FriendlyByteBuf buf, BiConsumer<Option<?>, FriendlyByteBuf> optionConsumer) {
        var optionKey = new Option.Key(entryKey);
        var option = config.optionForKey(optionKey);
        if (option == null) {
            Owo.LOGGER.error("Received override for unknown option '{}' in config '{}', skipping", optionKey, config.name());
            return;
        }
        optionConsumer.accept(option, buf);
    }

    @Environment(EnvType.CLIENT)
    private static boolean isSingleplayer(CommonAccess<?, ?, ?> access) {
        return access.runtime() instanceof Minecraft client && client.hasSingleplayerServer() && client.getSingleplayerServer().isSingleplayer();
    }

    private static void applyClient(ConfigsSyncPacket payload, CommonAccess<?, ?, ?> access) {
        Owo.LOGGER.info("Applying server overrides");
        var mismatchedOptions = new HashMap<Option<?>, Object>();

        if (!isSingleplayer(access)) {
            read(payload, (option, packetByteBuf) -> {
                var mismatchedValue = option.read(packetByteBuf);
                if (mismatchedValue != null) mismatchedOptions.put(option, mismatchedValue);
            });

            if (!mismatchedOptions.isEmpty()) {
                Owo.LOGGER.error("Aborting connection, non-syncable config values were mismatched");
                mismatchedOptions.forEach((option, serverValue) -> {
                    Owo.LOGGER.error("- Option {} in config '{}' has value '{}' but server requires '{}'",
                            option.key().asString(), option.configName(), option.value(), serverValue);
                });

                var errorMessage = Component.empty();
                var optionsByConfig = HashMultimap.<String, Tuple<Option<?>, Object>>create();

                mismatchedOptions.forEach((option, serverValue) -> optionsByConfig.put(option.configName(), new Tuple<>(option, serverValue)));
                for (var configName : optionsByConfig.keys()) {
                    errorMessage.append(TextOps.withFormatting("in config ", ChatFormatting.GRAY)).append(configName).append("\n");
                    for (var option : optionsByConfig.get(configName)) {
                        errorMessage.append(Component.translatable(option.getA().translationKey()).withStyle(ChatFormatting.YELLOW)).append(" -> ");
                        errorMessage.append(option.getA().value().toString()).append(TextOps.withFormatting(" (client)", ChatFormatting.GRAY));
                        errorMessage.append(TextOps.withFormatting(" / ", ChatFormatting.DARK_GRAY));
                        errorMessage.append(option.getB().toString()).append(TextOps.withFormatting(" (server)", ChatFormatting.GRAY)).append("\n");
                    }
                    errorMessage.append("\n");
                }

                errorMessage.append(TextOps.withFormatting("these options could not be synchronized because\n", ChatFormatting.GRAY));
                errorMessage.append(TextOps.withFormatting("they require your client to be restarted\n", ChatFormatting.GRAY));
                errorMessage.append(TextOps.withFormatting("change them manually and restart if you want to join this server", ChatFormatting.GRAY));

                access.connection().disconnect(TextOps.concat(PREFIX, errorMessage));
                return;
            }
        }

        Owo.LOGGER.info("Responding with client values");
        access.responseHandle().send(toPacket(Option.SyncMode.INFORM_SERVER));
    }

    private static void applyServer(ConfigsSyncPacket payload, ServerAccess access) {
        Owo.LOGGER.info("Receiving client config");
        var connection = access.connection();
        read(payload, (option, optionBuf) -> writeEntry(connection, option, optionBuf));
        ON_INITIAL_SYNC.invoker().onClientSync(access.player());
    }

    private static void writeEntry(Connection connection, Option<?> option, FriendlyByteBuf optionBuf) {
        var config = CLIENT_OPTION_STORAGE.computeIfAbsent(connection, _ -> new HashMap<>()).computeIfAbsent(option.configName(), _ -> new HashMap<>());
        if (config.containsKey(option.key())) {
            config.get(option.key()).set(optionBuf.read(option.endec()));
        } else {
            config.put(option.key(), Observable.of(optionBuf.read(option.endec())));
        }
    }

    @ApiStatus.Internal
    public record ConfigsSyncPacket(Map<String, ConfigEntry> configs) { }

    @ApiStatus.Internal
    public record ConfigEntry(Map<String, FriendlyByteBuf> options) { }

    @ApiStatus.Internal
    public record ConfigSyncPacket(String configName, String entryKey, FriendlyByteBuf buf) { }

    @ApiStatus.Internal
    public static void init() {
        var earlyPhase = Owo.id("early");
        ServerPlayConnectionEvents.JOIN.addPhaseOrdering(earlyPhase, Event.DEFAULT_PHASE);
        ServerPlayConnectionEvents.JOIN.register(earlyPhase, (handler, _, _) -> {
            Owo.LOGGER.info("Sending server config values to client");

            Owo.MAIN.serverHandle(handler).send(toPacket(Option.SyncMode.OVERRIDE_CLIENT));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((listener, _) -> {
            CLIENT_OPTION_STORAGE.remove(listener.getPacketContext().get(PacketContext.CONNECTION));
        });

        Owo.MAIN.registerBidirectional(ConfigsSyncPacket.class, ConfigSynchronizer::applyServer, ConfigSynchronizer::applyClient);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
                KNOWN_CONFIGS.values().forEach((config) -> config.forEachOption(Option::reattach));
            });
        }

        Owo.MAIN.registerServerbound(ConfigSyncPacket.class, (message, access) -> {
            var config = getConfig(message.configName());
            if (config == null) return;
            handleEntry(config, message.entryKey(), message.buf(), (option, optionBuf) -> writeEntry(access.connection(), option, optionBuf));
        });
    }
}
