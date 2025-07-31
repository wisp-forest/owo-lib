package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.util.FramebufferOverride;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPhase.class)
public class RenderPhaseMixin {

    @ModifyExpressionValue(method = "method_62272", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;"))
    private static Framebuffer injectProperRenderTarget(Framebuffer original) {
        if (FramebufferOverride.top() != null) {
            return FramebufferOverride.top();
        }

        return original;
    }

    @Inject(method = "method_29377", at = @At(value = "HEAD"), cancellable = true)
    private static void injectProperRenderTarget(CallbackInfo ci) {
        if (FramebufferOverride.top() == null) {
            return;
        }

        FramebufferOverride.top().beginWrite(true);
        ci.cancel();
    }

}
