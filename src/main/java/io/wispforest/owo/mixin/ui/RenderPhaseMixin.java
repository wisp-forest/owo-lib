package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ui.container.RenderEffectWrapper;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPhase.class)
public class RenderPhaseMixin {

    @ModifyExpressionValue(method = "method_62272", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;"))
    private static Framebuffer injectProperRenderTarget(Framebuffer original) {
        if (RenderEffectWrapper.currentFramebuffer() != null) {
            return RenderEffectWrapper.currentFramebuffer();
        }

        return original;
    }

}
