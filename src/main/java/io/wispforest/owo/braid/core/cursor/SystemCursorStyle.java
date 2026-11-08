package io.wispforest.owo.braid.core.cursor;

import io.wispforest.owo.util.CursorUtil;

public final class SystemCursorStyle implements CursorStyle {
    public final int glfwId;

    SystemCursorStyle(int glfwId) {
        this.glfwId = glfwId;
    }

    @Override
    public long allocate() {
        return CursorUtil.allocateCursor(this.glfwId);
    }
}
