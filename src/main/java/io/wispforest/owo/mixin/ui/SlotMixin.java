package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.util.pond.OwoSlotExtension;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin implements OwoSlotExtension {

    @Unique
    private boolean owo$disabledOverride = false;

    @Unique
    private @Nullable PositionedRectangle owo$scissorArea = null;

    @Override
    public void owo$setDisabledOverride(boolean disabled) {
        this.owo$disabledOverride = disabled;
    }

    @Override
    public boolean owo$getDisabledOverride() {
        return this.owo$disabledOverride;
    }

    @Override
    public void owo$setScissorArea(@Nullable PositionedRectangle scissor) {
        this.owo$scissorArea = scissor;
    }

    @Override
    public @Nullable PositionedRectangle owo$getScissorArea() {
        return this.owo$scissorArea;
    }

    @Inject(method = "isActive", at = @At("TAIL"), cancellable = true)
    private void injectOverride(CallbackInfoReturnable<Boolean> cir) {
        if (!this.owo$disabledOverride) return;
        cir.setReturnValue(false);
    }
}
