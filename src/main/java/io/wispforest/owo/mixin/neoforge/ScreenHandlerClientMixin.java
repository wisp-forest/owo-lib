package io.wispforest.owo.mixin.neoforge;

import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerClientMixin implements OwoScreenHandlerExtension {
    public void owo$sendToServer(CustomPayload payload) {
        MinecraftClient.getInstance().getNetworkHandler().send(payload);
    }
}
