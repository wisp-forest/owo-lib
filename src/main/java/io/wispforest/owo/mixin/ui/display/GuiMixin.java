package io.wispforest.owo.mixin.ui.display;

import org.lwjgl.sdl.SDLMouse;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.SystemCursorStyle;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class GuiMixin {

    @ModifyExpressionValue(method = "extractCrosshair", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;CROSSHAIR_SPRITE:Lnet/minecraft/resources/Identifier;", opcode = Opcodes.GETSTATIC))
    private Identifier injectDisplayCrosshair(Identifier original) {
        if (BraidDisplayBinding.targetDisplay == null) return original;

        var cursorStyle = BraidDisplayBinding.targetDisplay.display().app.surface.currentCursorStyle();
        if (cursorStyle == io.wispforest.owo.braid.core.cursor.CursorStyle.NONE) return Owo.id("cursors/none");
        if (!(cursorStyle instanceof SystemCursorStyle systemStyle)) return original;

        return switch (systemStyle.glfwId) {
            case SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE -> Owo.id("cursors/nesw_resize");
            case SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE -> Owo.id("cursors/nwse_resize");
            case SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE -> Owo.id("cursors/vertical_resize");
            case SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE -> Owo.id("cursors/horizontal_resize");
            case SDLMouse.SDL_SYSTEM_CURSOR_MOVE -> Owo.id("cursors/all_resize");
            case SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR -> Owo.id("cursors/crosshair");
            case SDLMouse.SDL_SYSTEM_CURSOR_POINTER -> Owo.id("cursors/hand");
            default -> original;
        };
    }

}
