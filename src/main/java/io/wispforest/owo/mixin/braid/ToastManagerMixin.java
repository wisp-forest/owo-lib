package io.wispforest.owo.mixin.braid;

import io.wispforest.owo.braid.util.BraidToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

    @Shadow
    @Final
    private List<ToastManager.ToastInstance<?>> visibleToasts;

    @Inject(method = "method_61991", at = @At(value = "INVOKE", target = "Ljava/util/BitSet;clear(II)V"))
    private void disposeBraidToasts(MutableBoolean mutableBoolean, ToastManager.ToastInstance<?> entry, CallbackInfoReturnable<Boolean> cir) {
        if (entry.getToast() instanceof BraidToast toast) {
            toast.dispose();
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void disposeBraidToastsEpisode2(CallbackInfo ci) {
        for (var entry : this.visibleToasts) {
            if (entry.getToast() instanceof BraidToast toast) {
                toast.dispose();
            }
        }
    }
}
