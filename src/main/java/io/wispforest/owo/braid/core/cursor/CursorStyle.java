package io.wispforest.owo.braid.core.cursor;

import io.wispforest.owo.braid.core.LayoutAxis;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

public sealed interface CursorStyle permits SystemCursorStyle {
    CursorStyle NONE = new SystemCursorStyle(0);
    CursorStyle POINTER = new SystemCursorStyle(GLFW.GLFW_ARROW_CURSOR);
    CursorStyle TEXT = new SystemCursorStyle(GLFW.GLFW_IBEAM_CURSOR);
    CursorStyle HAND = new SystemCursorStyle(GLFW.GLFW_HAND_CURSOR);
    CursorStyle MOVE = new SystemCursorStyle(GLFW.GLFW_RESIZE_ALL_CURSOR);
    CursorStyle CROSSHAIR = new SystemCursorStyle(GLFW.GLFW_CROSSHAIR_CURSOR);
    CursorStyle HORIZONTAL_RESIZE = new SystemCursorStyle(GLFW.GLFW_HRESIZE_CURSOR);
    CursorStyle VERTICAL_RESIZE = new SystemCursorStyle(GLFW.GLFW_VRESIZE_CURSOR);
    CursorStyle NWSE_RESIZE = new SystemCursorStyle(GLFW.GLFW_RESIZE_NWSE_CURSOR);
    CursorStyle NESW_RESIZE = new SystemCursorStyle(GLFW.GLFW_RESIZE_NESW_CURSOR);
    CursorStyle NOT_ALLOWED = new SystemCursorStyle(GLFW.GLFW_NOT_ALLOWED_CURSOR);

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
