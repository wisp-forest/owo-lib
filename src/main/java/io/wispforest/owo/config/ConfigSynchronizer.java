package io.wispforest.owo.config;

import com.google.common.collect.HashMultimap;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ServerCommonNetworkHandlerAccessor;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
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
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    public static final Identifier CONFIG_SYNC_CHANNEL = new Identifier("owo", "config_sync");

    private static final Map<ClientConnection, Map<String, Map<Option.Key, Object>>> CLIENT_OPTION_STORAGE = new WeakHashMap<>();

    private static final Map<String, ConfigWrapper<?>> KNOWN_CONFIGS = new HashMap<>();
    private static final MutableText PREFIX = TextOps.concat(Owo.PREFIX, Text.of("§cunrecoverable config mismatch\n\n"));

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
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayerEntity player, String configName) {
        var storage = CLIENT_OPTION_STORAGE.get(((ServerCommonNetworkHandlerAccessor) player.networkHandler).owo$getConnection());
        if (storage == null) return null;

        return storage.get(configName);
    }

    /**
     * Safer, more clear version of {@link #getClientOptions(ServerPlayerEntity, String)} to
     * be used when the actual config wrapper is available
     *
     * @see #getClientOptions(ServerPlayerEntity, String)
     */
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayerEntity player, ConfigWrapper<?> config) {
        return getClientOptions(player, config.name());
    }

    private static ConfigSyncPacket toPacket(Option.SyncMode targetMode) {
        Map<String, ConfigEntry> configs = new HashMap<>();

        KNOWN_CONFIGS.forEach((configName, config) -> {
            var entry = new ConfigEntry(new HashMap<>());

            config.allOptions().forEach((key, option) -> {
                if (option.syncMode().ordinal() < targetMode.ordinal()) return;

                PacketByteBuf optionBuf = PacketByteBufs.create();
                option.write(optionBuf);

                entry.options().put(key.asString(), optionBuf);
            });

            configs.put(configName, entry);
        });

        return new ConfigSyncPacket(configs);
    }

    private static void read(ConfigSyncPacket packet, BiConsumer<Option<?>, PacketByteBuf> optionConsumer) {
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

        if (!(context.client().isIntegratedServerRunning() && context.client().getServer().isSingleplayer())) {
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
                var optionsByConfig = HashMultimap.<String, Pair<Option<?>, Object>>create();

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

                context.player().networkHandler.getConnection().disconnect(TextOps.concat(PREFIX, errorMessage));
                return;
            }
        }

        Owo.LOGGER.info("Responding with client values");
        context.responseSender().sendPacket(toPacket(Option.SyncMode.INFORM_SERVER));
    }

    private static void applyServer(ConfigSyncPacket payload, ServerPlayNetworking.Context context) {
        Owo.LOGGER.info("Receiving client config");
        var connection = ((ServerCommonNetworkHandlerAccessor) context.player().networkHandler).owo$getConnection();

        read(payload, (option, optionBuf) -> {
            var config = CLIENT_OPTION_STORAGE.computeIfAbsent(connection, $ -> new HashMap<>()).computeIfAbsent(option.configName(), s -> new HashMap<>());
            config.put(option.key(), optionBuf.read(option.endec()));
        });
    }

    private record ConfigSyncPacket(Map<String, ConfigEntry> configs) implements CustomPayload {
        public static final Id<ConfigSyncPacket> ID = new Id<>(CONFIG_SYNC_CHANNEL);
        public static final Endec<ConfigSyncPacket> ENDEC = StructEndecBuilder.of(
                ConfigEntry.ENDEC.mapOf().fieldOf("configs", ConfigSyncPacket::configs),
                ConfigSyncPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private record ConfigEntry(Map<String, PacketByteBuf> options) {
        public static final Endec<ConfigEntry> ENDEC = StructEndecBuilder.of(
                BuiltInEndecs.PACKET_BYTE_BUF.mapOf().fieldOf("options", ConfigEntry::options),
                ConfigEntry::new
        );
    }

    static {
        PayloadTypeRegistry.playS2C().register(ConfigSyncPacket.ID, ConfigSyncPacket.ENDEC.packetCodec());
        PayloadTypeRegistry.playC2S().register(ConfigSyncPacket.ID, ConfigSyncPacket.ENDEC.packetCodec());

        var earlyPhase = new Identifier("owo", "early");
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
