package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {

    private static final MatrixStack EMPTY_STACK = new MatrixStack();
    private static final Deque<PositionedRectangle> STACK = new ArrayDeque<>();

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
        final var newFrame = withGlTransform(x, y, width, height, matrices);

        if (STACK.isEmpty()) {
            STACK.push(newFrame);
        } else {
            var top = STACK.peek();
            STACK.push(top.intersection(newFrame));
        }

        applyState();
    }

    public static void pop() {
        if (STACK.isEmpty()) {
            throw new IllegalStateException("Cannot pop frame from empty scissor stack");
        }

        STACK.pop();

        if (STACK.isEmpty()) {
            var window = MinecraftClient.getInstance().getWindow();
            GL11.glScissor(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
        } else {
            applyState();
        }
    }

    private static void applyState() {
        if (!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) return;

        var newFrame = STACK.peek();
        var window = MinecraftClient.getInstance().getWindow();
        var scale = window.getScaleFactor();

        GL11.glScissor(
                (int) (newFrame.x() * scale),
                (int) (window.getFramebufferHeight() - (newFrame.y() * scale) - newFrame.height() * scale),
                (int) (newFrame.width() * scale),
                (int) (newFrame.height() * scale)
        );
    }

    public static void drawUnclipped(Runnable action) {
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        if (scissorEnabled) GlStateManager._disableScissorTest();
        action.run();
        if (scissorEnabled) GlStateManager._enableScissorTest();
    }

    public static boolean isVisible(int x, int y, @Nullable MatrixStack matrices) {
        var top = STACK.peek();
        if (top == null) return true;

        return top.intersects(
                withGlTransform(
                        x, y, 0, 0, matrices
                )
        );
    }

    public static boolean isVisible(Component component, @Nullable MatrixStack matrices) {
        var top = STACK.peek();
        if (top == null) return true;

        var margins = component.margins().get();
        return top.intersects(
                withGlTransform(
                        component.x() - margins.left(),
                        component.y() - margins.top(),
                        component.width() + margins.right(),
                        component.height() + margins.bottom(),
                        matrices
                )
        );
    }

    private static PositionedRectangle withGlTransform(int x, int y, int width, int height, @Nullable MatrixStack matrices) {
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

        return PositionedRectangle.of(x, y, width, height);
    }
}
