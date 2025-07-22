package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class CursorController {

    private final Map<CursorStyle, Long> cursors = new HashMap<>();
    private final long windowHandle;

    private CursorStyle lastCursorStyle = CursorStyle.NONE;
    private boolean disposed = false;

    public CursorController(long windowHandle) {
        this.windowHandle = windowHandle;
    }

    public CursorStyle currentStyle() {
        return this.lastCursorStyle;
    }

    public void setStyle(CursorStyle style) {
        if (this.disposed || this.lastCursorStyle == style) return;

        if (style == CursorStyle.NONE) {
            GLFW.glfwSetCursor(this.windowHandle, 0);
        } else {
            if (!this.cursors.containsKey(style)) {
                this.cursors.put(style, style.allocate());
            }

            GLFW.glfwSetCursor(this.windowHandle, this.cursors.get(style));
        }

        this.lastCursorStyle = style;
    }

    public void dispose() {
        if (this.disposed) return;

        for (var ptr : this.cursors.values()) {
            if (ptr == 0) return;
            GLFW.glfwDestroyCursor(ptr);
        }

        this.disposed = true;
    }
}
