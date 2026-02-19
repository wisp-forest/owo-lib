package io.wispforest.uwu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.Owo;
import com.mojang.blaze3d.opengl.GlRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlRenderPass.class)
public class GlRenderPassMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"))
    private static boolean adjustDevCheck(Operation<Boolean> original) {
        return original.call() || Owo.DEBUG;
    }
}
