package io.wispforest.owo.braid.util;

import io.wispforest.owo.braid.core.Surface;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;

public record BraidGuiRendererTargetOverride(Framebuffer framebuffer, Surface surface) {
    private static @Nullable BraidGuiRendererTargetOverride current = null;

    public static @Nullable BraidGuiRendererTargetOverride current() {
        return current;
    }

    public static void run(BraidGuiRendererTargetOverride override, Runnable fn) {
        try {
            current = override;
            fn.run();
        } finally {
            current = null;
        }
    }
}
