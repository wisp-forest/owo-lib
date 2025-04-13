package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.glfw.GLFW;

public sealed interface CursorStyle permits SystemCursorStyle {
    CursorStyle NONE = new SystemCursorStyle(0);
    CursorStyle POINTER = new SystemCursorStyle(GLFW.GLFW_ARROW_CURSOR);
    CursorStyle TEXT = new SystemCursorStyle(GLFW.GLFW_IBEAM_CURSOR);
    CursorStyle HAND = new SystemCursorStyle(GLFW.GLFW_HAND_CURSOR);
    CursorStyle MOVE = new SystemCursorStyle(GLFW.GLFW_RESIZABLE);
    CursorStyle HORIZONTAL_RESIZE = new SystemCursorStyle(GLFW.GLFW_HRESIZE_CURSOR);
    CursorStyle VERTICAL_RESIZE = new SystemCursorStyle(GLFW.GLFW_VRESIZE_CURSOR);
    CursorStyle NWSE_RESIZE = new SystemCursorStyle(GLFW.GLFW_RESIZE_NWSE_CURSOR);
    CursorStyle NESW_RESIZE = new SystemCursorStyle(GLFW.GLFW_RESIZE_NESW_CURSOR);

    long allocate();
}
