package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.sdl.SDLMouse;
import io.wispforest.owo.braid.core.LayoutAxis;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;

public sealed interface CursorStyle permits SystemCursorStyle {
    CursorStyle NONE = new SystemCursorStyle(0);
    CursorStyle POINTER = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT);
    CursorStyle TEXT = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_TEXT);
    CursorStyle HAND = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_POINTER);
    CursorStyle MOVE = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_MOVE);
    CursorStyle CROSSHAIR = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR);
    CursorStyle HORIZONTAL_RESIZE = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE);
    CursorStyle VERTICAL_RESIZE = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE);
    CursorStyle NWSE_RESIZE = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE);
    CursorStyle NESW_RESIZE = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE);
    CursorStyle NOT_ALLOWED = new SystemCursorStyle(SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED);

    long allocate();

    static CursorStyle forDraggingAlong(LayoutAxis axis, Matrix3x2f transform3x2) {
        // Extract the Z rotation from the transform
        var rotation = Math.atan2(transform3x2.m01, transform3x2.m11);

        // Convert to degrees
        rotation = Math.toDegrees(rotation);
        // apply axis adjustment
        if (axis == LayoutAxis.VERTICAL) rotation += 90;
        // Normalize to [0, 180) (because the cursors are symmetric)
        rotation = Mth.positiveModulo(rotation, 180);
        // Map to [0, 8)
        rotation /= 22.5;

        if (rotation < 1 || rotation >= 7) return HORIZONTAL_RESIZE;
        else if (rotation >= 3 && rotation < 5) return VERTICAL_RESIZE;
        else if (rotation >= 1 && rotation < 3) return NESW_RESIZE;
        else return NWSE_RESIZE;
    }
}
