package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.util.pond.OwoSlotExtension;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin implements OwoSlotExtension {

    @Unique
    private boolean owo$disabledOverride = false;

    @Override
    public void owo$setDisabledOverride(boolean disabled) {
        this.owo$disabledOverride = disabled;
    }

    @Override
    public boolean owo$getDisabledOverride() {
        return this.owo$disabledOverride;
    }

    @Inject(method = "isEnabled", at = @At("TAIL"), cancellable = true)
    private void injectOverride(CallbackInfoReturnable<Boolean> cir) {
        if (!this.owo$disabledOverride) return;
        cir.setReturnValue(false);
    }
}
