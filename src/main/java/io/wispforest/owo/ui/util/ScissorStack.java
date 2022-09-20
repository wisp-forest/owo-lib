package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {

    private static final MatrixStack EMPTY_STACK = new MatrixStack();
    private static final Deque<ScissorArea> STACK = new ArrayDeque<>();

    public static void pushDirect(int x, int y, int width, int height) {
        var window = MinecraftClient.getInstance().getWindow();
        var scale = window.getScaleFactor();

        push(
                (int) (x / scale),
                (int) (window.getScaledHeight() - (y / scale) - height / scale),
                (int) (width / scale),
                (int) (height / scale),
                null
        );
    }

    public static void push(int x, int y, int width, int height, @Nullable MatrixStack matrices) {
        if (matrices == null) matrices = EMPTY_STACK;

        matrices.push();
        matrices.multiplyPositionMatrix(RenderSystem.getModelViewMatrix());

        var root = new Vector4f(x, y, 0, 1);
        var end = new Vector4f(x + width, y + height, 0, 1);

        root.transform(matrices.peek().getPositionMatrix());
        end.transform(matrices.peek().getPositionMatrix());

        x = (int) root.getX();
        y = (int) root.getY();

        width = (int) Math.ceil(end.getX() - root.getX());
        height = (int) Math.ceil(end.getY() - root.getY());

        matrices.pop();

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

    public static boolean isVisible(Component component, @Nullable MatrixStack matrices) {
        var top = STACK.peek();
        if (top == null) return true;

        var margins = component.margins().get();
        var root = new Vector4f(component.x() - margins.left(), component.y() - margins.top(), 0, 1);
        var end = new Vector4f(component.x() + component.width() + margins.right(), component.y() + component.height() + margins.bottom(), 0, 1);

        if (matrices == null) matrices = EMPTY_STACK;

        matrices.push();
        matrices.multiplyPositionMatrix(RenderSystem.getModelViewMatrix());

        root.transform(matrices.peek().getPositionMatrix());
        end.transform(matrices.peek().getPositionMatrix());

        matrices.pop();

        return root.getX() < top.x + top.width
                && end.getX() > top.x
                && root.getY() < top.y + top.height
                && end.getY() > top.y;
    }

    private static void applyState() {
        var newFrame = STACK.peek();
        var window = MinecraftClient.getInstance().getWindow();
        var scale = window.getScaleFactor();

        GL11.glScissor(
                (int) (newFrame.x * scale),
                (int) (window.getFramebufferHeight() - (newFrame.y * scale) - newFrame.height * scale),
                (int) (newFrame.width * scale),
                (int) (newFrame.height * scale)
        );
    }

    private record ScissorArea(int x, int y, int width, int height) {}
}
