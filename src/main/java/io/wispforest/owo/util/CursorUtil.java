package io.wispforest.owo.util;

import com.mojang.blaze3d.GLFWErrorCapture;
import com.mojang.blaze3d.GLFWErrorScope;
import io.wispforest.owo.Owo;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

@ApiStatus.Internal
public final class CursorUtil {
    public static long allocateCursor(int shape) {
        var capture = new GLFWErrorCapture();
        try (var _ = new GLFWErrorScope(capture)) {
            return GLFW.glfwCreateStandardCursor(shape);
        } finally {
            for (var error : capture)
                Owo.LOGGER.warn("An error occurred while allocating cursor {}: {}", shape, error);
        }
    }
}
