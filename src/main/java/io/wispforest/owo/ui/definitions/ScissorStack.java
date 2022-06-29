package io.wispforest.owo.ui.definitions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.AffineTransformation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {

    private static final Deque<ScissorArea> STACK = new ArrayDeque<>();

    public static void push(int x, int y, int width, int height, @Nullable MatrixStack matrices) {
        if (matrices != null) {
            var transform = new AffineTransformation(matrices.peek().getPositionMatrix());
            x += transform.getTranslation().getX();
            y += transform.getTranslation().getY();

            width *= transform.getScale().getX();
            height *= transform.getScale().getY();
        }

        if (STACK.isEmpty()) {
            STACK.push(new ScissorArea(x, y, width, height));
        } else {
            var top = STACK.peek();

            // my brain is fucking dead on the floor
            // this code is really, really simple
            // and honestly quite obvious
            //
            // my brain did not agree
            // glisco, 2022
            int leftEdge = Math.max(top.x, x);
            int topEdge = Math.max(top.y, y);

            int rightEdge = Math.min(top.x + top.width, x + width);
            int bottomEdge = Math.min(top.y + top.height, y + height);

            STACK.push(new ScissorArea(
                    leftEdge,
                    topEdge,
                    Math.max(rightEdge - leftEdge, 0),
                    Math.max(bottomEdge - topEdge, 0)
            ));
        }

        var newFrame = STACK.peek();
        var scale = MinecraftClient.getInstance().getWindow().getScaleFactor();

        GL11.glScissor(
                (int) (newFrame.x * scale),
                (int) (MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (newFrame.y * scale) - newFrame.height * scale),
                (int) (newFrame.width * scale),
                (int) (newFrame.height * scale)
        );
    }

    public static void pop() {
        if (STACK.isEmpty()) {
            throw new IllegalStateException("Cannot pop frame from empty scissor stack");
        }

        var window = MinecraftClient.getInstance().getWindow();
        STACK.pop();

        if (STACK.isEmpty()) {
            GL11.glScissor(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
        } else {
            var scale = window.getScaleFactor();
            var top = STACK.peek();

            GL11.glScissor(
                    (int) (top.x * scale),
                    (int) (MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (top.y * scale) - top.height * scale),
                    (int) (top.width * scale),
                    (int) (top.height * scale)
            );
        }
    }

    private record ScissorArea(int x, int y, int width, int height) {}
}
