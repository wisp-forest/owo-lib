package io.wispforest.owo.config;

import io.wispforest.owo.Owo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    public static final Identifier CONFIG_SYNC_CHANNEL = new Identifier("owo", "config_sync");

    private static final Map<String, ConfigWrapper<?>> KNOWN_CONFIGS = new HashMap<>();

    static void register(ConfigWrapper<?> config) {
        KNOWN_CONFIGS.put(config.name(), config);
    }

    /**
     * Retrieve the options which the given player's client
     * sent to the server during config synchronization
     *
     * @param player     The player for which to retrieve the client values
     * @param configName The name of the config for which to retrieve values
     * @return The player's client's values of the given config values,
     * or {@code null} if no config with the given name was synced
     */
    public static @Nullable Map<Option.Key, ?> getClientOptions(ServerPlayerEntity player, String configName) {
        var storage = ((ServerPlayerEntityExtension) player).owo$optionStorage();
        if (storage == null) return null;

        return storage.get(configName);
    }

    private static void write(PacketByteBuf packet, Option.SyncMode targetMode) {
        packet.writeVarInt(KNOWN_CONFIGS.size());

        var configBuf = PacketByteBufs.create();
        var optionBuf = PacketByteBufs.create();

        KNOWN_CONFIGS.forEach((configName, config) -> {
            packet.writeString(configName);

            configBuf.resetReaderIndex().resetWriterIndex();
            configBuf.writeVarInt((int) config.allOptions().values().stream().filter(option -> option.syncMode().ordinal() >= targetMode.ordinal()).count());

            config.allOptions().forEach((key, option) -> {
                if (option.syncMode().ordinal() < targetMode.ordinal()) return;

                configBuf.writeString(key.asString());

                optionBuf.resetReaderIndex().resetWriterIndex();
                option.write(optionBuf);

                configBuf.writeVarInt(optionBuf.readableBytes());
                configBuf.writeBytes(optionBuf);
            });

            packet.writeVarInt(configBuf.readableBytes());
            packet.writeBytes(configBuf);
        });
    }

    private static void read(PacketByteBuf buf, BiConsumer<Option<?>, PacketByteBuf> optionConsumer) {
        int configCount = buf.readVarInt();
        for (int i = 0; i < configCount; i++) {
            var configName = buf.readString();
            var config = KNOWN_CONFIGS.get(configName);
            if (config == null) {
                Owo.LOGGER.error("Received overrides for unknown config '{}', skipping", configName);

                // skip size of current config
                buf.skipBytes(buf.readVarInt());
                continue;
            }

            // ignore size
            buf.readVarInt();

            int optionCount = buf.readVarInt();
            for (int j = 0; j < optionCount; j++) {
                var optionKey = new Option.Key(buf.readString());
                var option = config.optionForKey(optionKey);
                if (option == null) {
                    Owo.LOGGER.error("Received override for unknown option '{}' in config '{}', skipping", optionKey, configName);

                    // skip size of current option
                    buf.skipBytes(buf.readVarInt());
                    continue;
                }

                // ignore size
                buf.readVarInt();

                optionConsumer.accept(option, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private static void applyClient(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Owo.LOGGER.info("Applying server overrides");
        read(buf, Option::read);

        Owo.LOGGER.info("Responding with client values");
        var packet = PacketByteBufs.create();
        write(packet, Option.SyncMode.INFORM_SERVER);

        sender.sendPacket(CONFIG_SYNC_CHANNEL, packet);
    }

    private static void applyServer(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Owo.LOGGER.info("Receiving client config");
        var storage = ((ServerPlayerEntityExtension) player).owo$optionStorage();

        read(buf, (option, optionBuf) -> {
            var config = storage.computeIfAbsent(option.configName(), s -> new HashMap<>());
            config.put(option.key(), option.serializer().deserializer().apply(optionBuf));
        });
    }

    static {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.isSingleplayer()) return;
            Owo.LOGGER.info("Sending server config values to client");

            var packet = PacketByteBufs.create();
            write(packet, Option.SyncMode.OVERRIDE_CLIENT);

            sender.sendPacket(CONFIG_SYNC_CHANNEL, packet);
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(CONFIG_SYNC_CHANNEL, ConfigSynchronizer::applyClient);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                KNOWN_CONFIGS.forEach((name, config) -> config.forEachOption(Option::reattach));
            });
        }

        ServerPlayNetworking.registerGlobalReceiver(CONFIG_SYNC_CHANNEL, ConfigSynchronizer::applyServer);
    }

    public interface ServerPlayerEntityExtension {
        Map<String, Map<Option.Key, Object>> owo$optionStorage();
    }
}
