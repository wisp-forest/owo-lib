package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.CursorStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;

public class CursorAdapter {

    protected static final CursorStyle[] ACTIVE_STYLES = {CursorStyle.POINTER, CursorStyle.TEXT, CursorStyle.HAND, CursorStyle.MOVE};

    protected final EnumMap<CursorStyle, Long> cursors = new EnumMap<>(CursorStyle.class);
    protected final long windowHandle;
    protected CursorStyle lastCursorStyle = CursorStyle.POINTER;

    protected boolean disposed = false;

    protected CursorAdapter(long windowHandle) {
        this.windowHandle = windowHandle;
        for (var style : ACTIVE_STYLES) {
            this.cursors.put(style, GLFW.glfwCreateStandardCursor(style.glfw));
        }
    }

    public static CursorAdapter ofClientWindow() {
        return new CursorAdapter(MinecraftClient.getInstance().getWindow().getHandle());
    }

    public static CursorAdapter ofWindow(Window window) {
        return new CursorAdapter(window.getHandle());
    }

    public static CursorAdapter ofWindow(long windowHandle) {
        return new CursorAdapter(windowHandle);
    }

    public void applyStyle(CursorStyle style) {
        if (this.disposed || this.lastCursorStyle == style) return;

        if (style == CursorStyle.NONE) {
            GLFW.glfwSetCursor(this.windowHandle, 0);
        } else {
            GLFW.glfwSetCursor(this.windowHandle, this.cursors.get(style));
        }
        this.lastCursorStyle = style;
    }

    public void dispose() {
        if (this.disposed) return;

        this.cursors.values().forEach(GLFW::glfwDestroyCursor);
        this.disposed = true;
    }

}
