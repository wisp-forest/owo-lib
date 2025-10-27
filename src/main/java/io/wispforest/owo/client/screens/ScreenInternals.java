package io.wispforest.owo.client.screens;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.endec.Endec;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
public class ScreenInternals {
    public static final Identifier SYNC_PROPERTIES = Identifier.of("owo", "sync_screen_handler_properties");
    public static final Identifier HANDSHAKE_REQUEST = Identifier.of("owo", "request_screen_handler_messages");
    public static final Identifier HANDSHAKE_RESPONSE = Identifier.of("owo", "response_screen_handler_messages");

    public static void init() {
        var localPacketCodec = CodecUtils.toPacketCodec(LocalPacket.ENDEC);

        PayloadTypeRegistry.playS2C().register(LocalPacket.ID, localPacketCodec);
        PayloadTypeRegistry.playC2S().register(LocalPacket.ID, localPacketCodec);
        PayloadTypeRegistry.playS2C().register(SyncPropertiesPacket.ID, CodecUtils.toPacketCodec(SyncPropertiesPacket.ENDEC));

        PayloadTypeRegistry.playS2C().register(HandshakeRequest.ID, CodecUtils.toPacketCodec(HandshakeRequest.ENDEC));
        PayloadTypeRegistry.playC2S().register(HandshakeResponse.ID, CodecUtils.toPacketCodec(HandshakeResponse.ENDEC));

        ServerPlayNetworking.registerGlobalReceiver(LocalPacket.ID, (payload, context) -> {
            var screenHandler = context.player().currentScreenHandler;

            if (screenHandler == null) {
                Owo.LOGGER.error("Received local packet for null ScreenHandler");
                return;
            }

            ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, false);
        });

        ServerPlayNetworking.registerGlobalReceiver(HandshakeResponse.ID, (payload, context) -> {
            var screenHandler = context.player().currentScreenHandler;

            if (screenHandler == null) {
                Owo.LOGGER.error("[ScreenHandlerHandshake] Received handshake response for null ScreenHandler");
                return;
            }

            if (!payload.type().equals(screenHandler.getType())) {
                Owo.LOGGER.error("[ScreenHandlerHandshake] Received handshake response packet for different ScreenHandler type: [Expected Type: {}, Current Type: {}]", payload.type(), screenHandler.getType());
                return;
            }

            ((OwoScreenHandlerExtension) screenHandler).owo$verifyData(context.player(), payload.messageNames());
        });
    }

    public record LocalPacket(int packetId, PacketByteBuf payload) implements CustomPayload {
        public static final Id<LocalPacket> ID = new Id<>(Identifier.of("owo", "local_packet"));
        public static final Endec<LocalPacket> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("packetId", LocalPacket::packetId),
            MinecraftEndecs.PACKET_BYTE_BUF.fieldOf("payload", LocalPacket::payload),
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
            MinecraftEndecs.PACKET_BYTE_BUF.fieldOf("payload", SyncPropertiesPacket::payload),
            SyncPropertiesPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void attemptHandshake(ScreenHandlerType<?> type, ServerPlayerEntity player) {
        if (type == null) return;

        if (ServerPlayNetworking.canSend(player, OwoHandshake.OFF_CHANNEL_ID)) {
            Owo.LOGGER.info("[ScreenHandlerHandshake] Handshake disabled by client, skipping");
            return;
        }

        try {
            ServerPlayNetworking.send(player, new HandshakeRequest(type));
        } catch (Exception e) {
            Owo.LOGGER.error("[ScreenHandlerHandshake] Unable to Handshake check handler as getting the type encountered an error: ", e);
        }
    }

    private record HandshakeRequest(ScreenHandlerType<?> type) implements CustomPayload {
        public static final Id<HandshakeRequest> ID = new Id<>(HANDSHAKE_REQUEST);
        public static final Endec<HandshakeRequest> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.SCREEN_HANDLER).fieldOf("type", HandshakeRequest::type),
            HandshakeRequest::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private record HandshakeResponse(ScreenHandlerType<?> type, LinkedHashSet<String> messageNames) implements CustomPayload {
        public static final CustomPayload.Id<HandshakeResponse> ID = new CustomPayload.Id<>(HANDSHAKE_RESPONSE);
        public static final Endec<HandshakeResponse> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.SCREEN_HANDLER).fieldOf("type", HandshakeResponse::type),
            Endec.STRING.listOf().xmap(LinkedHashSet::new, ArrayList::new).fieldOf("message_names", HandshakeResponse::messageNames),
            HandshakeResponse::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void init() {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (screen instanceof ScreenHandlerProvider<?> handled)
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

            ClientPlayNetworking.registerGlobalReceiver(HandshakeRequest.ID, (payload, context) -> {
                var screenHandler = context.player().currentScreenHandler;

                if (screenHandler == null) {
                    Owo.LOGGER.error("[ScreenHandlerHandshake] Received handshake request packet for null ScreenHandler");
                    return;
                }

                if (!payload.type().equals(screenHandler.getType())) {
                    Owo.LOGGER.error("[ScreenHandlerHandshake] Received handshake request packet for different ScreenHandler type: [Expected Type: {}, Current Type: {}]", payload.type(), screenHandler.getType());
                    return;
                }

                context.responseSender().sendPacket(new HandshakeResponse(screenHandler.getType(), ((OwoScreenHandlerExtension) screenHandler).owo$gatherMessageNames()));
            });
        }
    }
}
