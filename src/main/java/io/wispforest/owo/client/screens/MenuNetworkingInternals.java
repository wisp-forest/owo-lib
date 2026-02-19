package io.wispforest.owo.client.screens;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MenuNetworkingInternals {
    public static final Identifier SYNC_PROPERTIES = Owo.id("sync_menu_properties");

    public static void init(PayloadRegistrar registrar) {
        var localPacketCodec = CodecUtils.toPacketCodec(LocalPacket.ENDEC);

        IPayloadHandler<LocalPacket> handler = (payload, context) -> {
            context.enqueueWork(() -> {
                var menu = context.player().containerMenu;

                if (menu == null) {
                    Owo.LOGGER.error("Received local packet for null ContainerMenu");
                    return;
                }

                ((OwoScreenHandlerExtension) screenHandler).owo$handlePacket(payload, context.player().getEntityWorld().isClient());
            });
        };

        registrar.playBidirectional(LocalPacket.ID, localPacketCodec, handler, handler);
        registrar.playToClient(SyncPropertiesPacket.ID, CodecUtils.toPacketCodec(SyncPropertiesPacket.ENDEC), (payload, context) -> {
            context.enqueueWork(() -> {
                var screenHandler = context.player().currentScreenHandler;

                if (screenHandler == null) {
                    Owo.LOGGER.error("Received sync properties packet for null ScreenHandler");
                    return;
                }

                ((OwoAbstractContainerMenuExtension) menu).owo$readPropertySync(payload);
            });
        });
    }

    public record LocalPacket(int packetId, FriendlyByteBuf payload) implements CustomPacketPayload {
        public static final Type<LocalPacket> ID = new Type<>(Owo.id("local_packet"));
        public static final Endec<LocalPacket> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("packetId", LocalPacket::packetId),
            MinecraftEndecs.FRIENDLY_BYTE_BUF.fieldOf("payload", LocalPacket::payload),
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
            MinecraftEndecs.FRIENDLY_BYTE_BUF.fieldOf("payload", SyncPropertiesPacket::payload),
            SyncPropertiesPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
