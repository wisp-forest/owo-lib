package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.mixin.ui.access.GuiRenderStateAccessor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {

    @Inject(method = "blurBeforeThisStratum", at = @At("HEAD"), cancellable = true)
    private void makeBlurIdempotent(CallbackInfo ci) {
        var self = (GuiRenderStateAccessor) this;
        if (self.owo$getFirstStratumAfterBlur() != Integer.MAX_VALUE) {
            ci.cancel();
        }
    }
}
