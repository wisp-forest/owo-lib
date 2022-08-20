package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.ui.core.Component;
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

        applyState();
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
            applyState();
        }
    }

    public static void drawUnclipped(Runnable action) {
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        if (scissorEnabled) GlStateManager._disableScissorTest();
        action.run();
        if (scissorEnabled) GlStateManager._enableScissorTest();
    }

    public static boolean isVisible(Component component) {
        var top = STACK.peek();
        if (top == null) return true;

        var margins = component.margins().get();
        return component.x() - margins.left() < top.x + top.width
                && component.x() + component.width() + margins.right() > top.x
                && component.y() - margins.top() < top.y + top.height
                && component.y() + component.height() + margins.bottom() > top.y;
    }

    private static void applyState() {
        var newFrame = STACK.peek();
        var scale = MinecraftClient.getInstance().getWindow().getScaleFactor();

        GL11.glScissor(
                (int) (newFrame.x * scale),
                (int) (MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (newFrame.y * scale) - newFrame.height * scale),
                (int) (newFrame.width * scale),
                (int) (newFrame.height * scale)
        );
    }

    private record ScissorArea(int x, int y, int width, int height) {}
}
