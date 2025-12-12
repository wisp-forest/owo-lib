package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"), cancellable = true)
    private void cancelClose(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().screen instanceof CommandOpenedScreen) {
            cir.setReturnValue(true);
        }
    }

}
