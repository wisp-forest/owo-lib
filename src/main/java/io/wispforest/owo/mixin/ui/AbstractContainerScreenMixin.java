package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlStateManager;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.util.pond.OwoSlotExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    @Unique
    private static boolean inOwoScreen = false;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "render", at = @At("HEAD"))
    private void captureOwoState(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        inOwoScreen = (Object) this instanceof BaseOwoContainerScreen<?, ?>;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void resetOwoState(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        inOwoScreen = false;
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void injectSlotScissors(GuiGraphics context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (!inOwoScreen) return;

        var scissorArea = ((OwoSlotExtension) slot).owo$getScissorArea();
        if (scissorArea == null) return;

        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(scissorArea.x(), scissorArea.y(), scissorArea.width(), scissorArea.height());
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void clearSlotScissors(GuiGraphics context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (!inOwoScreen) return;

        var scissorArea = ((OwoSlotExtension) slot).owo$getScissorArea();
        if (scissorArea == null) return;

        GlStateManager._disableScissorTest();
    }

    @ModifyVariable(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0), ordinal = 2)
    private int doNoThrow(int slotId, @Local() Slot slot) {
        return (((Object) this instanceof BaseOwoContainerScreen<?, ?>) && slot != null) ? slot.index : slotId;
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"), cancellable = true)
    private void closeIt(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof BaseOwoContainerScreen<?, ?>)) return;

        if (input.isEscape() && this.shouldCloseOnEsc()) {
            this.onClose();
            cir.setReturnValue(true);
        }
    }
}
