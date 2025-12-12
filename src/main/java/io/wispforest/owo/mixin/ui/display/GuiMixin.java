package io.wispforest.owo.mixin.ui.display;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.SystemCursorStyle;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;CROSSHAIR_SPRITE:Lnet/minecraft/resources/Identifier;"))
    private Identifier injectDisplayCrosshair(Identifier original) {
        if (BraidDisplayBinding.targetDisplay == null) return original;

        var cursorStyle = BraidDisplayBinding.targetDisplay.display().app.surface.currentCursorStyle();
        if (!(cursorStyle instanceof SystemCursorStyle systemStyle)) return original;

        return switch (systemStyle.glfwId) {
            case GLFW.GLFW_RESIZE_NESW_CURSOR -> Owo.id("cursors/nesw_resize");
            case GLFW.GLFW_RESIZE_NWSE_CURSOR -> Owo.id("cursors/nwse_resize");
            case GLFW.GLFW_VRESIZE_CURSOR -> Owo.id("cursors/vertical_resize");
            case GLFW.GLFW_HRESIZE_CURSOR -> Owo.id("cursors/horizontal_resize");
            case GLFW.GLFW_RESIZE_ALL_CURSOR -> Owo.id("cursors/all_resize");
            case GLFW.GLFW_CROSSHAIR_CURSOR -> Owo.id("cursors/crosshair");
            case GLFW.GLFW_HAND_CURSOR -> Owo.id("cursors/hand");
            case 0 -> Owo.id("cursors/none");
            default -> original;
        };
    }

}
