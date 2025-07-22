package io.wispforest.owo.util;

import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class FramebufferOverride {
    private static final Deque<Framebuffer> STACK = new ArrayDeque<>();

    public static void push(Framebuffer framebuffer) {
        STACK.push(framebuffer);
    }

    public static @Nullable Framebuffer top() {
        return !STACK.isEmpty() ? STACK.getFirst() : null;
    }

    public static @Nullable Framebuffer pop() {
        return !STACK.isEmpty() ? STACK.pop() : null;
    }
}
