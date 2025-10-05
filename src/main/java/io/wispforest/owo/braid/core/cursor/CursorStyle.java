package io.wispforest.owo.braid.core.cursor;

import io.wispforest.owo.braid.core.LayoutAxis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;

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

    static CursorStyle forDraggingAlong(LayoutAxis axis, Matrix4f transform) {
        // Extract the Z rotation from the transform
        var rotation = transform
            .getUnnormalizedRotation(new Quaterniond())
            .getEulerAnglesXYZ(new Vector3d()).z;

        // Convert to degrees
        rotation = Math.toDegrees(rotation);
        // apply axis adjustment
        if (axis == LayoutAxis.VERTICAL) rotation += 90;
        // Normalize to [0, 180) (because the cursors are symmetric)
        rotation = MathHelper.floorMod(rotation, 180);
        // Map to [0, 8)
        rotation /= 22.5;

        if (rotation < 1 || rotation >= 7) return HORIZONTAL_RESIZE;
        else if (rotation >= 3 && rotation < 5) return VERTICAL_RESIZE;
        else if (rotation >= 1 && rotation < 3) return NESW_RESIZE;
        else return NWSE_RESIZE;
    }
}
