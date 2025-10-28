package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), cancellable = true)
    private void cancelClose(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().currentScreen instanceof CommandOpenedScreen) {
            cir.setReturnValue(true);
        }
    }

}
