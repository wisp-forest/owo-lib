package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.mixin.ui.access.DrawContextAccessor;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public final class ScissorStack {

    private static final MatrixStack EMPTY_STACK = new MatrixStack();
    private static final Deque<PositionedRectangle> STACK = new ArrayDeque<>();

    private ScissorStack() {}

    public static void pushDirect(int x, int y, int width, int height) {
        var window = MinecraftClient.getInstance().getWindow();
        var scale = window.getScaleFactor();

        push(
                (int) (x / scale),
                (int) (window.getScaledHeight() - (y / scale) - height / scale),
                (int) (width / scale),
                (int) (height / scale),
                (MatrixStack) null
        );
    }

    public static void push(int x, int y, int width, int height, DrawContext context) {
        context.draw();

        push(x, y, width, height, context.getMatrices());
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
        applyState();
    }

    private static void applyState() {
        if (STACK.isEmpty()) {
            var window = MinecraftClient.getInstance().getWindow();
            GL11.glScissor(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
            return;
        }

        if (!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) return;

        var newFrame = STACK.peek();
        var window = MinecraftClient.getInstance().getWindow();
        var scale = window.getScaleFactor();

        GL11.glScissor(
                Math.max(0, (int) (newFrame.x() * scale)),
                Math.max((int) (window.getFramebufferHeight() - (newFrame.y() * scale) - newFrame.height() * scale), 0),
                Math.min(MathHelper.clamp((int) (newFrame.width() * scale), 0, window.getFramebufferWidth()), window.getFramebufferWidth()),
                Math.min(MathHelper.clamp((int) (newFrame.height() * scale), 0, window.getFramebufferHeight()), window.getFramebufferHeight())
        );
    }

    public static void drawUnclipped(Runnable action) {
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        if (scissorEnabled) GlStateManager._disableScissorTest();
        action.run();
        if (scissorEnabled) GlStateManager._enableScissorTest();
    }

    public static void popFramesAndDraw(int maxPopFrames, Runnable action) {
        var previousFrames = new ArrayList<PositionedRectangle>();
        while (maxPopFrames > 1 && STACK.size() > 1) {
            previousFrames.add(0, STACK.pop());
            maxPopFrames--;
        }

        applyState();
        action.run();

        previousFrames.forEach(STACK::push);
        applyState();
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

        root.mul(matrices.peek().getPositionMatrix());
        end.mul(matrices.peek().getPositionMatrix());

        x = (int) root.x;
        y = (int) root.y;

        width = (int) Math.ceil(end.x - root.x);
        height = (int) Math.ceil(end.y - root.y);

        matrices.pop();

        return PositionedRectangle.of(x, y, width, height);
    }
}
