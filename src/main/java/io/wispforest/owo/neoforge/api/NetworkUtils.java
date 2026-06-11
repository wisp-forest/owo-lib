package io.wispforest.owo.neoforge.api;

import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class NetworkUtils {
    @Environment(EnvType.CLIENT)
    public static void sendToServer(CustomPacketPayload payload) {
        Minecraft.getInstance().getConnection().send(payload);
    }
}
