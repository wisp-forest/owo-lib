package io.wispforest.owo.config;

import com.google.common.collect.HashMultimap;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.config.base.SyncMode;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.mixin.ServerCommonNetworkHandlerAccessor;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.endec.Endec;
import io.wispforest.owo.packets.OwoPackets;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO: ADD API HOOK FOR WHEN A CLIENT VALUE HAS BEEN UPDATED AND SENT TO THE SERVER
public class ConfigSynchronizer {

    private static final Map<ClientConnection, Map<Identifier, Map<Key, Object>>> CLIENT_OPTION_STORAGE = new WeakHashMap<>();

    private static final Map<Identifier, ConfigWrapper<?>> KNOWN_CONFIGS = new HashMap<>();
    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§cunrecoverable config mismatch\n\n"));

    static void register(ConfigWrapper<?> config) {
        KNOWN_CONFIGS.put(config.id(), config);
    }

    /**
     * Retrieve the options which the given player's client
     * sent to the server during config synchronization
     *
     * @param player     The player for which to retrieve the client values
     * @param configId The name of the config for which to retrieve values
     * @return The player's client's values of the given config options,
     * or {@code null} if no config with the given name was synced
     */
    public static @Nullable Map<Key, ?> getClientOptions(ServerPlayerEntity player, Identifier configId) {
        var storage = CLIENT_OPTION_STORAGE.get(((ServerCommonNetworkHandlerAccessor) player.networkHandler).owo$getConnection());
        if (storage == null) return null;

        return storage.get(configId);
    }

    /**
     * Safer, more clear version of {@link #getClientOptions(ServerPlayerEntity, Identifier)} to
     * be used when the actual config wrapper is available
     *
     * @see #getClientOptions(ServerPlayerEntity, Identifier)
     */
    public static @Nullable Map<Key, ?> getClientOptions(ServerPlayerEntity player, ConfigWrapper<?> config) {
        return getClientOptions(player, config.id());
    }

    private static ConfigSyncPacket toPacket(Identifier configId, SyncMode targetMode) {
        var config = KNOWN_CONFIGS.get(configId);

        if (config == null) {
            throw new IllegalStateException("Unable to sync a reloaded config due to it not being registered for Config Synchronization!");
        }

        var entry = new ConfigEntry(new HashMap<>());

        config.allOptions().forEach((key, option) -> {
            if (option.syncMode().ordinal() < targetMode.ordinal()) return;

            PacketByteBuf optionBuf = PacketByteBufs.create();
            option.write(optionBuf);

            entry.options().put(key.asString(), optionBuf);
        });

        return new ConfigSyncPacket(Map.of(configId, entry));
    }

    private static ConfigSyncPacket toPacket(SyncMode targetMode) {
        Map<Identifier, ConfigEntry> configs = new HashMap<>();

        KNOWN_CONFIGS.forEach((configId, config) -> {
            var entry = new ConfigEntry(new HashMap<>());

            config.allOptions().forEach((key, option) -> {
                if (option.syncMode().ordinal() < targetMode.ordinal()) return;

                PacketByteBuf optionBuf = PacketByteBufs.create();
                option.write(optionBuf);

                entry.options().put(key.asString(), optionBuf);
            });

            configs.put(configId, entry);
        });

        return new ConfigSyncPacket(configs);
    }

    private static void read(Map<Identifier, ConfigEntry> configs, BiConsumer<FieldOption<?>, PacketByteBuf> optionConsumer) {
        for (var entry : configs.entrySet()) {
            var configId = entry.getKey();
            var configEntry = entry.getValue();

            var config = KNOWN_CONFIGS.get(configId);
            if (config == null) {
                Owo.LOGGER.error("Received overrides for unknown config '{}', skipping", configId);
                return;
            }

            for (var optionEntry : configEntry.options().entrySet()) {
                var optionKey = new Key(optionEntry.getKey());
                var option = config.optionForKey(optionKey);
                if (option == null) {
                    Owo.LOGGER.error("Received override for unknown option '{}' in config '{}', skipping", optionKey, configId);
                    return;
                }

                optionConsumer.accept(option, optionEntry.getValue());
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private static void applyOverridesAndSendOptions(ConfigSyncPacket packet, ClientAccess access) {
        Owo.LOGGER.info("Applying server overrides");
        var mismatchedOptions = new HashMap<FieldOption<?>, Object>();

        var configs = packet.configs();

        if (!(access.runtime().isIntegratedServerRunning() && access.runtime().getServer().isSingleplayer())) {
            read(configs, (option, packetByteBuf) -> {
                var mismatchedValue = option.read(packetByteBuf);
                if (mismatchedValue != null) mismatchedOptions.put(option, mismatchedValue);
            });

            if (!mismatchedOptions.isEmpty()) {
                Owo.LOGGER.error("Aborting connection, non-syncable config values were mismatched");
                mismatchedOptions.forEach((option, serverValue) -> {
                    Owo.LOGGER.error("- Option {} in config '{}' has value '{}' but server requires '{}'",
                            option.key().asString(), option.configName(), option.value(), serverValue);
                });

                var errorMessage = Text.empty();
                var optionsByConfig = HashMultimap.<String, Pair<FieldOption<?>, Object>>create();

                mismatchedOptions.forEach((option, serverValue) -> optionsByConfig.put(option.configName(), new Pair<>(option, serverValue)));
                for (var configName : optionsByConfig.keys()) {
                    errorMessage.append(TextOps.withFormatting("in config ", Formatting.GRAY)).append(configName).append("\n");
                    for (var option : optionsByConfig.get(configName)) {
                        errorMessage.append(Text.translatable(option.getLeft().translationKey()).formatted(Formatting.YELLOW)).append(" -> ");
                        errorMessage.append(option.getLeft().value().toString()).append(TextOps.withFormatting(" (client)", Formatting.GRAY));
                        errorMessage.append(TextOps.withFormatting(" / ", Formatting.DARK_GRAY));
                        errorMessage.append(option.getRight().toString()).append(TextOps.withFormatting(" (server)", Formatting.GRAY)).append("\n");
                    }
                    errorMessage.append("\n");
                }

                errorMessage.append(TextOps.withFormatting("these options could not be synchronized because\n", Formatting.GRAY));
                errorMessage.append(TextOps.withFormatting("they require your client to be restarted\n", Formatting.GRAY));
                errorMessage.append(TextOps.withFormatting("change them manually and restart if you want to join this server", Formatting.GRAY));

                access.player().networkHandler.getConnection().disconnect(TextOps.concat(PREFIX, errorMessage));
                return;
            }
        }

        Owo.LOGGER.info("Responding with client values");

        var syncPacket = configs.size() == 1
                ? toPacket(List.copyOf(configs.keySet()).getFirst(), SyncMode.INFORM_SERVER)
                : toPacket(SyncMode.INFORM_SERVER);

        OwoPackets.MAIN.clientHandle().send(syncPacket);
    }

    private static void handleClientConfigs(ConfigSyncPacket packet, ServerAccess access) {
        Owo.LOGGER.info("Receiving client config");
        var connection = ((ServerCommonNetworkHandlerAccessor) access.player().networkHandler).owo$getConnection();

        read(packet.configs(), (option, optionBuf) -> {
            var config = CLIENT_OPTION_STORAGE.computeIfAbsent(connection, $ -> new HashMap<>()).computeIfAbsent(option.configId(), s -> new HashMap<>());
            config.put(option.key(), optionBuf.read(option.endec()));
        });
    }

    private record ConfigSyncPacket(Map<Identifier, ConfigEntry> configs) {
        public static final StructEndec<ConfigSyncPacket> ENDEC = StructEndecBuilder.of(
                Endec.map(Identifier::toString, Identifier::of, ConfigEntry.ENDEC).fieldOf("configs", ConfigSyncPacket::configs),
                ConfigSyncPacket::new
        );
    }

    private record ConfigEntry(Map<String, PacketByteBuf> options) {
        public static final Endec<ConfigEntry> ENDEC = StructEndecBuilder.of(
                MinecraftEndecs.PACKET_BYTE_BUF.mapOf().fieldOf("options", ConfigEntry::options),
                ConfigEntry::new
        );
    }

    public static void sendLoadedServerConfig(Identifier configId) {
        var server = Owo.currentServer();

        if (server == null) return;

        if (!KNOWN_CONFIGS.containsKey(configId)) return;

        Owo.LOGGER.info("Resending server config values to client");

        OwoPackets.MAIN.serverHandle(server).send(toPacket(configId, SyncMode.OVERRIDE_CLIENT));
    }

    @Environment(EnvType.CLIENT)
    public static void sendChangedConfigValues(Identifier configId) {
        var player = MinecraftClient.getInstance().player;

        if (player == null || !KNOWN_CONFIGS.containsKey(configId)) return;

        Owo.LOGGER.info("Sending client config values to server");

        OwoPackets.MAIN.clientHandle().send(toPacket(configId, SyncMode.INFORM_SERVER));
    }

    public static void initNetworking() {
        OwoPackets.MAIN.registerServerbound(ConfigSyncPacket.class, ConfigSyncPacket.ENDEC, ConfigSynchronizer::handleClientConfigs);
        OwoPackets.MAIN.registerClientboundDeferred(ConfigSyncPacket.class, ConfigSyncPacket.ENDEC);

        var earlyPhase = Identifier.of("owo", "early");
        ServerPlayConnectionEvents.JOIN.addPhaseOrdering(earlyPhase, Event.DEFAULT_PHASE);
        ServerPlayConnectionEvents.JOIN.register(earlyPhase, (handler, sender, server) -> {
            Owo.LOGGER.info("Sending server config values to client");

            OwoPackets.MAIN.serverHandle(handler.getPlayer()).send(toPacket(SyncMode.OVERRIDE_CLIENT));
        });
    }

    public static void initClientNetworking() {
        OwoPackets.MAIN.registerClientbound(ConfigSyncPacket.class, ConfigSyncPacket.ENDEC, ConfigSynchronizer::applyOverridesAndSendOptions);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            KNOWN_CONFIGS.forEach((name, config) -> config.forEachOption(FieldOption::reattach));
        });
    }
}
