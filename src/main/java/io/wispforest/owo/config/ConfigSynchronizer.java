package io.wispforest.owo.config;

import com.google.common.collect.HashMultimap;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ServerCommonPacketListenerImplAccessor;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//import net.fabricmc.fabric.api.event.Event;
import io.wispforest.owo.neoforge.api.FriendlyByteBufs;
//import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    private static final Map<Connection, Map<String, Map<Option.Key, Object>>> CLIENT_OPTION_STORAGE = new WeakHashMap<>();

    private static final Map<String, ConfigWrapper<?>> KNOWN_CONFIGS = new HashMap<>();
    private static final MutableComponent PREFIX = TextOps.concat(Owo.PREFIX, Component.nullToEmpty("§cunrecoverable config mismatch\n\n"));

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
        var storage = CLIENT_OPTION_STORAGE.get(((ServerCommonPacketListenerImplAccessor) player.connection).owo$getConnection());
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

                FriendlyByteBuf optionBuf = FriendlyByteBufs.create();
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
    private static void applyClient(ConfigSyncPacket payload, ClientAccess context) {
        Owo.LOGGER.info("Applying server overrides");
        var mismatchedOptions = new HashMap<Option<?>, Object>();

        if (!(context.runtime().hasSingleplayerServer() && context.runtime().getSingleplayerServer().isSingleplayer())) {
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

                ((LocalPlayer) context.player()).connection.getConnection().disconnect(TextOps.concat(PREFIX, errorMessage));
                return;
            }
        }

        Owo.LOGGER.info("Responding with client values");
        context.channel().clientHandle().send(toPacket(Option.SyncMode.INFORM_SERVER));
    }

    private static void applyServer(ConfigSyncPacket payload, ServerAccess access) {
        Owo.LOGGER.info("Receiving client config");
        var connection = ((ServerCommonPacketListenerImplAccessor) access.player().connection).owo$getConnection();

        read(payload, (option, optionBuf) -> {
            var config = CLIENT_OPTION_STORAGE.computeIfAbsent(connection, _ -> new HashMap<>()).computeIfAbsent(option.configName(), _ -> new HashMap<>());
            config.put(option.key(), optionBuf.read(option.endec()));
        });
    }

    @ApiStatus.Internal
    public record ConfigSyncPacket(Map<String, ConfigEntry> configs) { }

    @ApiStatus.Internal
    public record ConfigEntry(Map<String, FriendlyByteBuf> options) { }

    @ApiStatus.Internal
    public static void init() {
        NeoForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(EventPriority.HIGHEST, (event) -> {
            Owo.LOGGER.info("Sending server config values to client");

            Owo.MAIN.serverHandle(event.getEntity()).send(toPacket(Option.SyncMode.OVERRIDE_CLIENT));
        });

        if (FMLEnvironment.getDist() == Dist.CLIENT) initClient();
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener((event) -> {
            KNOWN_CONFIGS.forEach((name, config) -> config.forEachOption(Option::reattach));
        });
    }
}
