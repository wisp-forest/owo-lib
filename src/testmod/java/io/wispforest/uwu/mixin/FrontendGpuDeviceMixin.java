package io.wispforest.uwu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import io.wispforest.owo.Owo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FrontendGpuDevice.class)
public class FrontendGpuDeviceMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"))
    private static boolean adjustDevCheck(Operation<Boolean> original) {
        return original.call() || Owo.DEBUG;
    }
}