package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.ui.core.CursorStyle;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLMouse;

import java.util.EnumMap;

public class CursorAdapter {

    protected static final CursorStyle[] ACTIVE_STYLES = {CursorStyle.POINTER, CursorStyle.TEXT, CursorStyle.HAND, CursorStyle.CROSSHAIR, CursorStyle.MOVE, CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE, CursorStyle.NWSE_RESIZE, CursorStyle.NESW_RESIZE, CursorStyle.NOT_ALLOWED};

    protected final EnumMap<CursorStyle, Long> cursors = new EnumMap<>(CursorStyle.class);
    protected final long windowHandle;

    protected CursorStyle lastCursorStyle = CursorStyle.POINTER;
    protected boolean disposed = false;

    protected CursorAdapter(long windowHandle) {
        this.windowHandle = windowHandle;
        for (var style : ACTIVE_STYLES) {
            var pointer = SDLMouse.SDL_CreateSystemCursor(style.glfw);
            if (pointer == 0) continue;

            this.cursors.put(style, pointer);
        }
    }

    public static CursorAdapter ofClientWindow() {
        return new CursorAdapter(Minecraft.getInstance().getWindow().handle());
    }

    public static CursorAdapter ofWindow(Window window) {
        return new CursorAdapter(window.handle());
    }

    public static CursorAdapter ofWindow(long windowHandle) {
        return new CursorAdapter(windowHandle);
    }

    public void applyStyle(CursorStyle style) {
        if (this.disposed) return;
        this.lastCursorStyle = style;
        if (SDLMouse.SDL_GetMouseFocus() != this.windowHandle) return;

        if (style == CursorStyle.NONE) {
            SDLMouse.SDL_SetCursor(SDLMouse.SDL_GetDefaultCursor());
        } else {
            SDLMouse.SDL_SetCursor(this.cursors.getOrDefault(style, SDLMouse.SDL_GetDefaultCursor()));
        }
        this.lastCursorStyle = style;
    }

    public void dispose() {
        if (this.disposed) return;

        SDLMouse.SDL_SetCursor(SDLMouse.SDL_GetDefaultCursor());
        this.cursors.values().forEach(SDLMouse::SDL_DestroyCursor);
        this.disposed = true;
    }

}
