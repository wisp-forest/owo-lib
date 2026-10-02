package io.wispforest.owo.braid.core.cursor;

import org.lwjgl.sdl.SDLMouse;

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
            if (SDLMouse.SDL_GetMouseFocus() == this.windowHandle) SDLMouse.SDL_SetCursor(SDLMouse.SDL_GetDefaultCursor());
        } else {
            if (!this.cursors.containsKey(style)) {
                this.cursors.put(style, style.allocate());
            }

            if (SDLMouse.SDL_GetMouseFocus() == this.windowHandle) SDLMouse.SDL_SetCursor(this.cursors.get(style));
        }

        this.lastCursorStyle = style;
    }

    public void apply() {
        if (this.disposed) return;
        SDLMouse.SDL_SetCursor(this.cursors.getOrDefault(this.lastCursorStyle, SDLMouse.SDL_GetDefaultCursor()));
    }

    public void dispose() {
        if (this.disposed) return;

        SDLMouse.SDL_SetCursor(SDLMouse.SDL_GetDefaultCursor());
        for (var ptr : this.cursors.values()) {
            if (ptr == 0) continue;
            SDLMouse.SDL_DestroyCursor(ptr);
        }

        this.disposed = true;
    }
}
