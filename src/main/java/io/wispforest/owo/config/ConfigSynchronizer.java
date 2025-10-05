package io.wispforest.owo.config;

import com.google.common.collect.HashMultimap;
import io.netty.buffer.Unpooled;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ServerCommonNetworkHandlerAccessor;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    public static final Identifier CONFIG_SYNC_CHANNEL = Identifier.of("owo", "config_sync");

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

                PacketByteBuf optionBuf = new PacketByteBuf(Unpooled.buffer());
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

    //@OnlyIn(Dist.CLIENT)
    private static void applyClient(ConfigSyncPacket payload, IPayloadContext context) {
        if (!FMLLoader.getCurrent().getDist().isClient()) throw new IllegalStateException("Unable to execute applyClient as currently its not a CLIENT Dist!");
        var client = MinecraftClient.getInstance();

        Owo.LOGGER.info("Applying server overrides");
        var mismatchedOptions = new HashMap<Option<?>, Object>();

        if (!(client.isIntegratedServerRunning() && client.getServer().isSingleplayer())) {
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

                ((ClientPlayerEntity) context.player()).networkHandler.getConnection().disconnect(TextOps.concat(PREFIX, errorMessage));
                return;
            }
        }

        Owo.LOGGER.info("Responding with client values");
        context.reply(toPacket(Option.SyncMode.INFORM_SERVER));
    }

    private static void applyServer(ConfigSyncPacket payload, IPayloadContext context) {
        Owo.LOGGER.info("Receiving client config");
        var connection = ((ServerCommonNetworkHandlerAccessor) ((ServerPlayerEntity) context.player()).networkHandler).owo$getConnection();

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
                MinecraftEndecs.PACKET_BYTE_BUF.mapOf().fieldOf("options", ConfigEntry::options),
                ConfigEntry::new
        );
    }

    public static void register(PayloadRegistrar registrar) {
        var packetCodec = CodecUtils.toPacketCodec(ConfigSyncPacket.ENDEC);

        IPayloadHandler<ConfigSyncPacket> handler = (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player().getEntityWorld().isClient()) {
                    ConfigSynchronizer.applyClient(payload, context);
                } else {
                    ConfigSynchronizer.applyServer(payload, context);
                }
            });
        };

        registrar.playBidirectional(ConfigSyncPacket.ID, packetCodec, handler);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (OnDatapackSyncEvent event) -> {
            if (event.getPlayer() != null) {
                Owo.LOGGER.info("Sending server config values to client");

                event.getPlayer().networkHandler.send(toPacket(Option.SyncMode.OVERRIDE_CLIENT));
            }
        });
    }

    public static void onDisconnect() {
        if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) KNOWN_CONFIGS.forEach((name, config) -> config.forEachOption(Option::reattach));
    }
}
