package io.wispforest.owo.util;

import org.lwjgl.glfw.GLFW;

public final class OwoGlfwUtil {
    private OwoGlfwUtil() {

    }

    public static ContextRestorer setContext(long handle) {
        long old = GLFW.glfwGetCurrentContext();
        if (old == handle) return new ContextRestorer(-1);

        GLFW.glfwMakeContextCurrent(handle);

        return new ContextRestorer(old);
    }

    public static class ContextRestorer implements AutoCloseable {
        private final long oldContext;

        private ContextRestorer(long old) {
            this.oldContext = old;
        }

        @Override
        public void close() {
            if (oldContext == -1) return;

            GLFW.glfwMakeContextCurrent(oldContext);
        }
    }
}
