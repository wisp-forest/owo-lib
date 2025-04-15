package io.wispforest.owo.braid.core.cursor;

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
}
