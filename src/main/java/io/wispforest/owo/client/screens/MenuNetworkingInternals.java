package io.wispforest.owo.client.screens;

import io.wispforest.endec.annotations.IsVarInt;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.CommonAccess;
import io.wispforest.owo.util.pond.OwoAbstractContainerMenuExtension;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class MenuNetworkingInternals {
    public static void init() {
        Owo.MAIN.registerClientboundCommon(SyncPropertiesPacket.class, (payload, access) -> {
            var ext = getExtension(access, "sync properties");
            if (ext != null) ext.owo$readPropertySync(payload);
        });
        Owo.MAIN.registerBidirectional(LocalPacket.class, (payload, access) -> {
            var ext = getExtension(access, "local packet");
            if (ext != null) ext.owo$handlePacket(payload, access.player().level().isClientSide());
        });
    }

    private static @Nullable OwoAbstractContainerMenuExtension getExtension(CommonAccess access, String packetType) {
        if (access.player().containerMenu instanceof OwoAbstractContainerMenuExtension ext) return ext;
        Owo.LOGGER.error("Received {} packet for null ContainerMenu", packetType);
        return null;
    }

    public record LocalPacket(@IsVarInt int packetId, FriendlyByteBuf payload){ }

    public record SyncPropertiesPacket(FriendlyByteBuf payload) { }
}
