package io.wispforest.owo.ui.window;

import net.minecraft.client.MinecraftClient;

public final class CurrentWindowContext {
    private static OwoWindow<?> CURRENT = null;

    private CurrentWindowContext() {

    }

    static WindowResetter setCurrent(OwoWindow<?> window) {
        var old = CURRENT;
        CURRENT = window;
        return new WindowResetter(old);
    }

    public static boolean isMain() {
        return CURRENT == null;
    }

    public static int framebufferWidth() {
        if (CURRENT != null) {
            return CURRENT.width();
        } else {
            return MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        }
    }

    public static int scaledWidth() {
        if (CURRENT != null) {
            return CURRENT.scaledWidth();
        } else {
            return MinecraftClient.getInstance().getWindow().getScaledWidth();
        }
    }

    public static int framebufferHeight() {
        if (CURRENT != null) {
            return CURRENT.height();
        } else {
            return MinecraftClient.getInstance().getWindow().getFramebufferHeight();
        }
    }

    public static int scaledHeight() {
        if (CURRENT != null) {
            return CURRENT.scaledHeight();
        } else {
            return MinecraftClient.getInstance().getWindow().getScaledHeight();
        }
    }

    public static double scaleFactor() {
        if (CURRENT != null) {
            return CURRENT.scaleFactor();
        } else {
            return MinecraftClient.getInstance().getWindow().getScaleFactor();
        }
    }

    public static long handle() {
        if (CURRENT != null) {
            return CURRENT.handle();
        } else {
            return MinecraftClient.getInstance().getWindow().getHandle();
        }
    }

    public static class WindowResetter implements AutoCloseable {
        private final OwoWindow<?> window;

        private WindowResetter(OwoWindow<?> window) {
            this.window = window;
        }

        @Override
        public void close() {
            CurrentWindowContext.CURRENT = window;
        }
    }
}
