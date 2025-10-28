package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Component;
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
import java.util.function.Supplier;

public final class ScissorStack {

    private static final MatrixStack EMPTY_STACK = new MatrixStack();
    private static final Deque<PositionedRectangle> STACK = new ArrayDeque<>();

    // TODO: this is a horrible klduge. braid needs to start supplying a custom draw context
    //  that also includes a surface-aware scissor stack
    private static final Deque<Supplier<ViewportDimensions>> VIEWPORT_DIMENSIONS_SUPPLIERS = new ArrayDeque<>();

    static {
        VIEWPORT_DIMENSIONS_SUPPLIERS.push(() -> {
            var window = MinecraftClient.getInstance().getWindow();
            return new ViewportDimensions(
                window.getScaleFactor(),
                window.getScaledWidth(),
                window.getScaledHeight(),
                window.getFramebufferWidth(),
                window.getFramebufferHeight()
            );
        });
    }

    private ScissorStack() {}

    public static void pushViewportDimensions(Supplier<ViewportDimensions> supplier) {
        VIEWPORT_DIMENSIONS_SUPPLIERS.push(supplier);
        applyState();
    }

    public static void popViewportDimensions() {
        VIEWPORT_DIMENSIONS_SUPPLIERS.pop();
        applyState();
    }

    private static ViewportDimensions currentDimensions() {
        return VIEWPORT_DIMENSIONS_SUPPLIERS.getFirst().get();
    }

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
            var dimensions = currentDimensions();
            STACK.push(newFrame.intersection(PositionedRectangle.of(0, 0, dimensions.scaledWidth(), dimensions.scaledHeight())));
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
            var dimensions = currentDimensions();
            GL11.glScissor(0, 0, dimensions.framebufferWidth(), dimensions.framebufferHeight());
            return;
        }

        if (!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) return;

        var newFrame = STACK.peek();
        var dimensions = currentDimensions();
        var scale = dimensions.scaleFactor;

        GL11.glScissor(
            Math.max(0, (int) (newFrame.x() * scale)),
            Math.max((int) (dimensions.framebufferHeight() - (newFrame.y() * scale) - newFrame.height() * scale), 0),
            Math.min(MathHelper.clamp((int) (newFrame.width() * scale), 0, dimensions.framebufferWidth()), dimensions.framebufferWidth()),
            Math.min(MathHelper.clamp((int) (newFrame.height() * scale), 0, dimensions.framebufferHeight()), dimensions.framebufferHeight())
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

        var tl = new Vector4f(x, y, 0, 1).mul(matrices.peek().getPositionMatrix());
        var tr = new Vector4f(x + width, y, 0, 1).mul(matrices.peek().getPositionMatrix());
        var bl = new Vector4f(x, y + height, 0, 1).mul(matrices.peek().getPositionMatrix());
        var br = new Vector4f(x + width, y + height, 0, 1).mul(matrices.peek().getPositionMatrix());

        var x1 = Math.min(tl.x, Math.min(tr.x, Math.min(bl.x, br.x)));
        var x2 = Math.max(tl.x, Math.max(tr.x, Math.max(bl.x, br.x)));
        var y1 = Math.min(tl.y, Math.min(tr.y, Math.min(bl.y, br.y)));
        var y2 = Math.max(tl.y, Math.max(tr.y, Math.max(bl.y, br.y)));

        matrices.pop();

        return PositionedRectangle.of((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1));
    }

    public record ViewportDimensions(double scaleFactor, int scaledWidth, int scaledHeight, int framebufferWidth, int framebufferHeight) {}
}
