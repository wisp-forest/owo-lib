package io.wispforest.owo.client.screens;

import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ScreenInternals {
    public static final Identifier SYNC_PROPERTIES = Identifier.of("owo", "sync_screen_handler_properties");

    public static void init() {
        var localPacketCodec = CodecUtils.toPacketCodec(LocalPacket.ENDEC);

        PayloadTypeRegistry.playS2C().register(LocalPacket.ID, localPacketCodec);
        PayloadTypeRegistry.playC2S().register(LocalPacket.ID, localPacketCodec);
        PayloadTypeRegistry.playS2C().register(SyncPropertiesPacket.ID, CodecUtils.toPacketCodec(SyncPropertiesPacket.ENDEC));

        ServerPlayNetworking.registerGlobalReceiver(LocalPacket.ID, (payload, context) -> {
            var screenHandler = context.player().containerMenu;

            if (screenHandler == null) {
                Owo.LOGGER.error("Received local packet for null ScreenHandler");
                return;
            }

            ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, false);
        });
    }

    public record LocalPacket(int packetId, FriendlyByteBuf payload) implements CustomPacketPayload {
        public static final Type<LocalPacket> ID = new Type<>(Identifier.of("owo", "local_packet"));
        public static final Endec<LocalPacket> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("packetId", LocalPacket::packetId),
            MinecraftEndecs.PACKET_BYTE_BUF.fieldOf("payload", LocalPacket::payload),
            LocalPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncPropertiesPacket(FriendlyByteBuf payload) implements CustomPacketPayload {
        public static final Type<SyncPropertiesPacket> ID = new Type<>(SYNC_PROPERTIES);
        public static final Endec<SyncPropertiesPacket> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.PACKET_BYTE_BUF.fieldOf("payload", SyncPropertiesPacket::payload),
            SyncPropertiesPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void init() {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof MenuAccess<?> handled)
                    ((OwoScreenHandlerExtension) handled.getMenu()).owo$attachToPlayer(client.player);
            });

            ClientPlayNetworking.registerGlobalReceiver(LocalPacket.ID, (payload, context) -> {
                var screenHandler = context.player().containerMenu;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received local packet for null ScreenHandler");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, true);
            });

            ClientPlayNetworking.registerGlobalReceiver(SyncPropertiesPacket.ID, (payload, context) -> {
                var screenHandler = context.player().containerMenu;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received sync properties packet for null ScreenHandler");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$readPropertySync(payload);
            });
        }
    }
}
