package io.wispforest.owo.config;

import com.google.common.collect.HashMultimap;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.Option.Key;
import io.wispforest.owo.mixin.ServerCommonNetworkHandlerAccessor;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.TextFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableText;
import net.minecraft.network.chat.Text;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    public static final Identifier CONFIG_SYNC_CHANNEL = Identifier.of("owo", "config_sync");

    private static final Map<Connection, Map<String, Map<Option.Key, Object>>> CLIENT_OPTION_STORAGE = new WeakHashMap<>();

    private static final Map<String, ConfigWrapper<?>> KNOWN_CONFIGS = new HashMap<>();
    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.nullToEmpty("§cunrecoverable config mismatch\n\n"));

    static void register(ConfigWrapper<?> config) {
        KNOWN_CONFIGS.put(config.name(), config);
    }

    /**
     * Retrieve the options which the given player's client
     * sent to the server during config synchronization
     *
     * @param player     The player for which to retrieve the client values
     * @param configName The name of the config for which to retrieve values
     * @return The player's client's values of the given config options,
     * or {@code null} if no config with the given name was synced
     */
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayer player, String configName) {
        var storage = CLIENT_OPTION_STORAGE.get(((ServerCommonNetworkHandlerAccessor) player.connection).owo$getConnection());
        if (storage == null) return null;

        return storage.get(configName);
    }

    /**
     * Safer, more clear version of {@link #getClientOptions(ServerPlayer, String)} to
     * be used when the actual config wrapper is available
     *
     * @see #getClientOptions(ServerPlayer, String)
     */
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayer player, ConfigWrapper<?> config) {
        return getClientOptions(player, config.name());
    }

    private static ConfigSyncPacket toPacket(Option.SyncMode targetMode) {
        Map<String, ConfigEntry> configs = new HashMap<>();

        KNOWN_CONFIGS.forEach((configName, config) -> {
            var entry = new ConfigEntry(new HashMap<>());

            config.allOptions().forEach((key, option) -> {
                if (option.syncMode().ordinal() < targetMode.ordinal()) return;

                FriendlyByteBuf optionBuf = PacketByteBufs.create();
                option.write(optionBuf);

                entry.options().put(key.asString(), optionBuf);
            });

            configs.put(configName, entry);
        });

        return new ConfigSyncPacket(configs);
    }

    private static void read(ConfigSyncPacket packet, BiConsumer<Option<?>, FriendlyByteBuf> optionConsumer) {
        for (var configEntry : packet.configs().entrySet()) {
            var configName = configEntry.getKey();
            var config = KNOWN_CONFIGS.get(configName);
            if (config == null) {
                Owo.LOGGER.error("Received overrides for unknown config '{}', skipping", configName);
                continue;
            }

            for (var optionEntry : configEntry.getValue().options().entrySet()) {
                var optionKey = new Option.Key(optionEntry.getKey());
                var option = config.optionForKey(optionKey);
                if (option == null) {
                    Owo.LOGGER.error("Received override for unknown option '{}' in config '{}', skipping", optionKey, configName);
                    continue;
                }

                optionConsumer.accept(option, optionEntry.getValue());
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private static void applyClient(ConfigSyncPacket payload, ClientPlayNetworking.Context context) {
        Owo.LOGGER.info("Applying server overrides");
        var mismatchedOptions = new HashMap<Option<?>, Object>();

        if (!(context.client().hasSingleplayerServer() && context.client().getSingleplayerServer().isSingleplayer())) {
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

                var errorMessage = Text.empty();
                var optionsByConfig = HashMultimap.<String, Tuple<Option<?>, Object>>create();

                mismatchedOptions.forEach((option, serverValue) -> optionsByConfig.put(option.configName(), new Tuple<>(option, serverValue)));
                for (var configName : optionsByConfig.keys()) {
                    errorMessage.append(TextOps.withFormatting("in config ", TextFormatting.GRAY)).append(configName).append("\n");
                    for (var option : optionsByConfig.get(configName)) {
                        errorMessage.append(Text.translatable(option.getA().translationKey()).withStyle(TextFormatting.YELLOW)).append(" -> ");
                        errorMessage.append(option.getA().value().toString()).append(TextOps.withFormatting(" (client)", TextFormatting.GRAY));
                        errorMessage.append(TextOps.withFormatting(" / ", TextFormatting.DARK_GRAY));
                        errorMessage.append(option.getB().toString()).append(TextOps.withFormatting(" (server)", TextFormatting.GRAY)).append("\n");
                    }
                    errorMessage.append("\n");
                }

                errorMessage.append(TextOps.withFormatting("these options could not be synchronized because\n", TextFormatting.GRAY));
                errorMessage.append(TextOps.withFormatting("they require your client to be restarted\n", TextFormatting.GRAY));
                errorMessage.append(TextOps.withFormatting("change them manually and restart if you want to join this server", TextFormatting.GRAY));

                context.player().connection.getConnection().disconnect(TextOps.concat(PREFIX, errorMessage));
                return;
            }
        }

        Owo.LOGGER.info("Responding with client values");
        context.responseSender().sendPacket(toPacket(Option.SyncMode.INFORM_SERVER));
    }

    private static void applyServer(ConfigSyncPacket payload, ServerPlayNetworking.Context context) {
        Owo.LOGGER.info("Receiving client config");
        var connection = ((ServerCommonNetworkHandlerAccessor) context.player().connection).owo$getConnection();

        read(payload, (option, optionBuf) -> {
            var config = CLIENT_OPTION_STORAGE.computeIfAbsent(connection, $ -> new HashMap<>()).computeIfAbsent(option.configName(), s -> new HashMap<>());
            config.put(option.key(), optionBuf.read(option.endec()));
        });
    }

    private record ConfigSyncPacket(Map<String, ConfigEntry> configs) implements CustomPacketPayload {
        public static final Type<ConfigSyncPacket> ID = new Type<>(CONFIG_SYNC_CHANNEL);
        public static final Endec<ConfigSyncPacket> ENDEC = StructEndecBuilder.of(
                ConfigEntry.ENDEC.mapOf().fieldOf("configs", ConfigSyncPacket::configs),
                ConfigSyncPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    private record ConfigEntry(Map<String, FriendlyByteBuf> options) {
        public static final Endec<ConfigEntry> ENDEC = StructEndecBuilder.of(
                MinecraftEndecs.PACKET_BYTE_BUF.mapOf().fieldOf("options", ConfigEntry::options),
                ConfigEntry::new
        );
    }

    static {
        var packetCodec = CodecUtils.toPacketCodec(ConfigSyncPacket.ENDEC);

        PayloadTypeRegistry.playS2C().register(ConfigSyncPacket.ID, packetCodec);
        PayloadTypeRegistry.playC2S().register(ConfigSyncPacket.ID, packetCodec);

        var earlyPhase = Identifier.of("owo", "early");
        ServerPlayConnectionEvents.JOIN.addPhaseOrdering(earlyPhase, Event.DEFAULT_PHASE);
        ServerPlayConnectionEvents.JOIN.register(earlyPhase, (handler, sender, server) -> {
            Owo.LOGGER.info("Sending server config values to client");

            sender.sendPacket(toPacket(Option.SyncMode.OVERRIDE_CLIENT));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPacket.ID, ConfigSynchronizer::applyClient);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                KNOWN_CONFIGS.forEach((name, config) -> config.forEachOption(Option::reattach));
            });
        }

        ServerPlayNetworking.registerGlobalReceiver(ConfigSyncPacket.ID, ConfigSynchronizer::applyServer);
    }
}
