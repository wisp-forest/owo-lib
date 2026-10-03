package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.sdl.SDLMouse;

public final class SystemCursorStyle implements CursorStyle {
    static final int NONE_ID = -1;

    public final int sdlId;

    SystemCursorStyle(int sdlId) {
        this.sdlId = sdlId;
    }

    @Override
    public long allocate() {
        if (this.sdlId == NONE_ID) {
            return 0;
        } else {
            return SDLMouse.SDL_CreateSystemCursor(this.sdlId);
        }
    }
}
