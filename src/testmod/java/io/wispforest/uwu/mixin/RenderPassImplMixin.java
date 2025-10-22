package io.wispforest.uwu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.Owo;
import net.minecraft.client.gl.RenderPassImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPassImpl.class)
public class RenderPassImplMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;isDevelopment:Z"))
    private static boolean adjustDevCheck(Operation<Boolean> original) {
        return original.call() || Owo.DEBUG;
    }
}
