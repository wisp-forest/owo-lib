package io.wispforest.owo.mixin.ui.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow private int activeButton;

    @Inject(method = "method_55795", at = @At("HEAD"), cancellable = true)
    private void captureScreenMouseDrag(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, CallbackInfo ci) {
        boolean handled = false;
        for (var instance : Layers.getInstances(screen)) {
            handled = instance.adapter.mouseDragged(mouseX, mouseY, this.activeButton, deltaX, deltaY);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Unique
    private static final ThreadLocal<Integer> MODS = new ThreadLocal<>();

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void captureMods(long window, int button, int action, int mods, CallbackInfo ci) {
        MODS.set(mods);
    }

    @WrapOperation(method = "method_1611", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
    private static boolean passModifiersToBraid$mouseClicked(Screen screen, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        if (screen instanceof BraidScreen braidScreen) return braidScreen.mouseClicked(mouseX, mouseY, button, MODS.get());
        return original.call(screen, mouseX, mouseY, button);
    }

    @WrapOperation(method = "method_1605", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"))
    private static boolean passModifiersToBraid$mouseReleased(Screen screen, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        if (screen instanceof BraidScreen braidScreen) return braidScreen.mouseReleased(mouseX, mouseY, button, MODS.get());
        return original.call(screen, mouseX, mouseY, button);
    }
}
