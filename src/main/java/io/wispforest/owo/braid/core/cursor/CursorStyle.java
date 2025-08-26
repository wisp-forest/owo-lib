package io.wispforest.owo.braid.core.cursor;

import io.wispforest.owo.braid.core.LayoutAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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

    static CursorStyle forDraggingAlong(LayoutAxis axis, Matrix4f transform) {
        var rotation = transform.getUnnormalizedRotation(new Quaternionf());

        var transformedAxis = rotation.transform(
            new Vector3f(axis.choose(1, 0), axis.choose(0, 1), 0),
            new Vector3f()
        );

        var angle = (Math.toDegrees(Math.atan2(transformedAxis.y, transformedAxis.x)) + 360) % 360;

        if ((angle >= 337.5 || angle < 22.5) || (angle >= 157.5 && angle < 202.5)) {
            return HORIZONTAL_RESIZE;
        } else if ((angle >= 67.5 && angle < 112.5) || (angle >= 247.5 && angle < 292.5)) {
            return VERTICAL_RESIZE;
        } else if ((angle >= 22.5 && angle < 67.5) || (angle >= 202.5 && angle < 247.5)) {
            return NESW_RESIZE;
        } else {
            return NWSE_RESIZE;
        }
    }
}
