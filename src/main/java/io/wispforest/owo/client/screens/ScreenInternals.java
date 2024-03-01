package io.wispforest.owo.client.screens;

import io.wispforest.owo.Owo;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ScreenInternals {
    public static final Identifier SYNC_PROPERTIES = new Identifier("owo", "sync_screen_handler_properties");

    public static void init() {
        PayloadTypeRegistry.playS2C().register(LocalPacket.ID, LocalPacket.ENDEC.packetCodec());
        PayloadTypeRegistry.playC2S().register(LocalPacket.ID, LocalPacket.ENDEC.packetCodec());
        PayloadTypeRegistry.playS2C().register(SyncPropertiesPacket.ID, SyncPropertiesPacket.ENDEC.packetCodec());

        ServerPlayNetworking.registerGlobalReceiver(LocalPacket.ID, (payload, context) -> {
            var screenHandler = context.player().currentScreenHandler;

            if (screenHandler == null) {
                Owo.LOGGER.error("Received local packet for null ScreenHandler");
                return;
            }

            ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, false);
        });
    }

    public record LocalPacket(int packetId, PacketByteBuf payload) implements CustomPayload {
        public static final Id<LocalPacket> ID = new Id<>(new Identifier("owo", "local_packet"));
        public static final Endec<LocalPacket> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("packetId", LocalPacket::packetId),
            BuiltInEndecs.PACKET_BYTE_BUF.fieldOf("payload", LocalPacket::payload),
            LocalPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SyncPropertiesPacket(PacketByteBuf payload) implements CustomPayload {
        public static final Id<SyncPropertiesPacket> ID = new Id<>(SYNC_PROPERTIES);
        public static final Endec<SyncPropertiesPacket> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.PACKET_BYTE_BUF.fieldOf("payload", SyncPropertiesPacket::payload),
            SyncPropertiesPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void init() {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof HandledScreen<?> handled)
                    ((OwoScreenHandlerExtension) handled.getScreenHandler()).owo$attachToPlayer(client.player);
            });

            ClientPlayNetworking.registerGlobalReceiver(LocalPacket.ID, (payload, context) -> {
                var screenHandler = context.player().currentScreenHandler;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received local packet for null ScreenHandler");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, true);
            });

            ClientPlayNetworking.registerGlobalReceiver(SyncPropertiesPacket.ID, (payload, context) -> {
                var screenHandler = context.player().currentScreenHandler;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received sync properties packet for null ScreenHandler");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$readPropertySync(payload);
            });
        }
    }
}
