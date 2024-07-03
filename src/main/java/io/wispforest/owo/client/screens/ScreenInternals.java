package io.wispforest.owo.client.screens;

import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.endec.Endec;
import io.wispforest.owo.extras.network.NetworkDirection;
import io.wispforest.owo.extras.network.OwoInternalNetworking;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ScreenInternals {
    public static final Identifier SYNC_PROPERTIES = Identifier.of("owo", "sync_screen_handler_properties");

    public static void init() {
        var localPacketCodec = LocalPacket.ENDEC;

        OwoInternalNetworking.registerPayloadType(NetworkDirection.S2C, NetworkPhase.PLAY, LocalPacket.ID, localPacketCodec);
        OwoInternalNetworking.registerPayloadType(NetworkDirection.C2S, NetworkPhase.PLAY, LocalPacket.ID, localPacketCodec);
        OwoInternalNetworking.registerPayloadType(NetworkDirection.S2C, NetworkPhase.PLAY, SyncPropertiesPacket.ID, SyncPropertiesPacket.ENDEC);

        OwoInternalNetworking.registerReceiver(NetworkDirection.C2S, NetworkPhase.PLAY, LocalPacket.ID, (payload, context) -> {
            var screenHandler = context.player().currentScreenHandler;

            if (screenHandler == null) {
                Owo.LOGGER.error("Received local packet for null ScreenHandler");
                return;
            }

            ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, false);
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

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public static void init() {
            NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
                var screen = event.getScreen();
                var client = MinecraftClient.getInstance();

                if (screen instanceof ScreenHandlerProvider<?> handled) ((OwoScreenHandlerExtension) handled.getScreenHandler()).owo$attachToPlayer(client.player);
            });

            OwoInternalNetworking.registerReceiver(NetworkDirection.S2C, NetworkPhase.PLAY, LocalPacket.ID, (payload, context) -> {
                var screenHandler = context.player().currentScreenHandler;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received local packet for null ScreenHandler");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, true);
            });

            OwoInternalNetworking.registerReceiver(NetworkDirection.S2C, NetworkPhase.PLAY, SyncPropertiesPacket.ID, (payload, context) -> {
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
