package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.sdl.SDLMouse;

public final class SystemCursorStyle implements CursorStyle {
    public final int glfwId;

    SystemCursorStyle(int glfwId) {
        this.glfwId = glfwId;
    }

    @Override
    public long allocate() {
        return this == CursorStyle.NONE ? 0 : SDLMouse.SDL_CreateSystemCursor(this.glfwId);
    }
}
