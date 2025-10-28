package io.wispforest.owo.mixin.braid;

import io.wispforest.owo.braid.util.BraidToast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
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
    private List<ToastManager.Entry<?>> visibleEntries;

    @Inject(method = "method_45074", at = @At(value = "INVOKE", target = "Ljava/util/BitSet;clear(II)V"))
    private void disposeBraidToasts(int i, DrawContext drawContext, ToastManager.Entry visibleEntry, CallbackInfoReturnable<Boolean> cir) {
        if (visibleEntry.getInstance() instanceof BraidToast toast) {
            toast.dispose();
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void disposeBraidToastsEpisode2(CallbackInfo ci) {
        for (var entry : this.visibleEntries) {
            if (entry.getInstance() instanceof BraidToast toast) {
                toast.dispose();
            }
        }
    }
}
