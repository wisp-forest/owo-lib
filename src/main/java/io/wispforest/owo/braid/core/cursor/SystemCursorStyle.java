package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.glfw.GLFW;

public final class SystemCursorStyle implements CursorStyle {
    public final int glfwId;

    SystemCursorStyle(int glfwId) {
        this.glfwId = glfwId;
    }

    @Override
    public long allocate() {
        return GLFW.glfwCreateStandardCursor(this.glfwId);
    }
}
