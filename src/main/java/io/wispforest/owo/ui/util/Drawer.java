package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.mixin.ui.ScreenInvoker;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of vanilla's {@link DrawableHelper} with all methods
 * statically accessible as well as extra convenience methods
 */
public class Drawer extends DrawableHelper {

    private static boolean recording = false;

    private static final Drawer INSTANCE = new Drawer();
    private final DebugDrawer debug = new DebugDrawer();

    public static final Identifier PANEL_TEXTURE = new Identifier("owo", "textures/gui/panel.png");
    public static final Identifier DARK_PANEL_TEXTURE = new Identifier("owo", "textures/gui/dark_panel.png");

    private Drawer() {}

    /**
     * Draw the outline of a rectangle
     *
     * @param matrices The transformation matrix stack
     * @param x        The x-coordinate of top-left corner of the rectangle
     * @param y        The y-coordinate of top-left corner of the rectangle
     * @param width    The width of the rectangle
     * @param height   The height of the rectangle
     * @param color    The color of the rectangle
     */
    public static void drawRectOutline(MatrixStack matrices, int x, int y, int width, int height, int color) {
        fill(matrices, x, y, x + width, y + 1, color);
        fill(matrices, x, y + height - 1, x + width, y + height, color);

        fill(matrices, x, y + 1, x + 1, y + height - 1, color);
        fill(matrices, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /**
     * Draw a filled rectangle with a gradient
     *
     * @param matrices         The transformation matrix stack
     * @param x                The x-coordinate of top-left corner of the rectangle
     * @param y                The y-coordinate of top-left corner of the rectangle
     * @param width            The width of the rectangle
     * @param height           The height of the rectangle
     * @param topLeftColor     The color at the rectangle's top left corner
     * @param topRightColor    The color at the rectangle's top right corner
     * @param bottomRightColor The color at the rectangle's bottom right corner
     * @param bottomLeftColor  The color at the rectangle's bottom left corner
     */
    public static void drawGradientRect(MatrixStack matrices, int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
        var buffer = Tessellator.getInstance().getBuffer();
        var matrix = matrices.peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x + width, y, 0).color(topRightColor).next();
        buffer.vertex(matrix, x, y, 0).color(topLeftColor).next();
        buffer.vertex(matrix, x, y + height, 0).color(bottomLeftColor).next();
        buffer.vertex(matrix, x + width, y + height, 0).color(bottomRightColor).next();

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator.getInstance().draw();

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    /**
     * Draw a panel that looks like the background of a vanilla
     * inventory screen
     *
     * @param matrices The transformation matrix stack
     * @param x        The x-coordinate of top-left corner of the panel
     * @param y        The y-coordinate of top-left corner of the panel
     * @param width    The width of the panel
     * @param height   The height of the panel
     * @param dark     Whether to use the dark version of the panel texture
     */
    public static void drawPanel(MatrixStack matrices, int x, int y, int width, int height, boolean dark) {
        (dark ? OwoNinePatchRenderers.DARK_PANEL : OwoNinePatchRenderers.LIGHT_PANEL).draw(matrices, x, y, width, height);
    }

    public static void drawText(MatrixStack matrices, Text text, float x, float y, float scale, int color) {
        drawText(matrices, text, x, y, scale, color, TextAnchor.TOP_LEFT);
    }

    public static void drawText(MatrixStack matrices, Text text, float x, float y, float scale, int color, TextAnchor anchorPoint) {
        final var textRenderer = MinecraftClient.getInstance().textRenderer;

        matrices.push();
        matrices.scale(scale, scale, 1);

        switch (anchorPoint) {
            case TOP_RIGHT -> x -= textRenderer.getWidth(text) * scale;
            case BOTTOM_LEFT -> y -= textRenderer.fontHeight * scale;
            case BOTTOM_RIGHT -> {
                x -= textRenderer.getWidth(text) * scale;
                y -= textRenderer.fontHeight * scale;
            }
        }

        textRenderer.draw(matrices, text, x * (1 / scale), y * (1 / scale), color);
        matrices.pop();
    }

    public static void drawTooltip(MatrixStack matrices, int x, int y, List<TooltipComponent> tooltip) {
        ((ScreenInvoker) utilityScreen()).owo$renderTooltipFromComponents(matrices, tooltip, x, y);
    }

    public static UtilityScreen utilityScreen() {
        return UtilityScreen.get();
    }

    public static DebugDrawer debug() {
        return INSTANCE.debug;
    }

    public static void recordQuads() {
        recording = true;
    }

    public static boolean recording() {
        return recording;
    }

    public static void submitQuads() {
        recording = false;
        Tessellator.getInstance().draw();
    }

    public enum TextAnchor {
        TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT
    }

    public static class DebugDrawer {

        private DebugDrawer() {}

        /**
         * Draw the area around the given rectangle which
         * the given insets describe
         *
         * @param matrices The transformation matrix stack
         * @param x        The x-coordinate of top-left corner of the rectangle
         * @param y        The y-coordinate of top-left corner of the rectangle
         * @param width    The width of the rectangle
         * @param height   The height of the rectangle
         * @param insets   The insets to draw around the rectangle
         * @param color    The color to draw the inset area with
         */
        public void drawInsets(MatrixStack matrices, int x, int y, int width, int height, Insets insets, int color) {
            fill(matrices, x - insets.left(), y - insets.top(), x + width + insets.right(), y, color);
            fill(matrices, x - insets.left(), y + height, x + width + insets.right(), y + height + insets.bottom(), color);

            fill(matrices, x - insets.left(), y, x, y + height, color);
            fill(matrices, x + width, y, x + width + insets.right(), y + height, color);
        }

        /**
         * Draw the element inspector for the given tree, detailing the position,
         * bounding box, margins and padding of each component
         *
         * @param matrices    The transformation matrix stack
         * @param root        The root component of the hierarchy to draw
         * @param mouseX      The x-coordinate of the mouse pointer
         * @param mouseY      The y-coordinate of the mouse pointer
         * @param onlyHovered Whether to only draw the inspector for the hovered widget
         */
        public void drawInspector(MatrixStack matrices, ParentComponent root, double mouseX, double mouseY, boolean onlyHovered) {
            RenderSystem.disableDepthTest();
            var client = MinecraftClient.getInstance();
            var textRenderer = client.textRenderer;

            var children = new ArrayList<Component>();
            if (!onlyHovered) {
                root.collectChildren(children);
            } else if (root.childAt((int) mouseX, (int) mouseY) != null) {
                children.add(root.childAt((int) mouseX, (int) mouseY));
            }

            for (var child : children) {
                if (child instanceof ParentComponent parentComponent) {
                    this.drawInsets(matrices, parentComponent.x(), parentComponent.y(), parentComponent.width(),
                            parentComponent.height(), parentComponent.padding().get().inverted(), 0xA70CECDD);
                }

                final var margins = child.margins().get();
                this.drawInsets(matrices, child.x(), child.y(), child.width(), child.height(), margins, 0xA7FFF338);
                drawRectOutline(matrices, child.x(), child.y(), child.width(), child.height(), 0xFF3AB0FF);

                if (onlyHovered) {

                    int inspectorX = child.x() + 1;
                    int inspectorY = child.y() + child.height() + child.margins().get().bottom() + 1;
                    int inspectorHeight = textRenderer.fontHeight * 2 + 4;

                    if (inspectorY > client.getWindow().getScaledHeight() - inspectorHeight) {
                        inspectorY -= child.fullSize().height() + inspectorHeight + 1;
                        if (inspectorY < 0) inspectorY = 1;
                        if (child instanceof ParentComponent parentComponent) {
                            inspectorX += parentComponent.padding().get().left();
                            inspectorY += parentComponent.padding().get().top();
                        }
                    }

                    final var nameText = Text.of(child.getClass().getSimpleName() + (child.id() != null ? " '" + child.id() + "'" : ""));
                    final var descriptor = Text.literal(child.x() + "," + child.y() + " (" + child.width() + "," + child.height() + ")"
                            + " <" + margins.top() + "," + margins.bottom() + "," + margins.left() + "," + margins.right() + "> ");
                    if (child instanceof ParentComponent parentComponent) {
                        var padding = parentComponent.padding().get();
                        descriptor.append(" >" + padding.top() + "," + padding.bottom() + "," + padding.left() + "," + padding.right() + "<");
                    }

                    int width = Math.max(textRenderer.getWidth(nameText), textRenderer.getWidth(descriptor));
                    fill(matrices, inspectorX, inspectorY, inspectorX + width + 3, inspectorY + inspectorHeight, 0xA7000000);
                    drawRectOutline(matrices, inspectorX, inspectorY, width + 3, inspectorHeight, 0xA7000000);

                    textRenderer.draw(matrices, nameText,
                            inspectorX + 2, inspectorY + 2, 0xFFFFFF);
                    textRenderer.draw(matrices, descriptor,
                            inspectorX + 2, inspectorY + textRenderer.fontHeight + 2, 0xFFFFFF);
                }
            }

            RenderSystem.enableDepthTest();
        }
    }

    public static class UtilityScreen extends Screen {

        private static UtilityScreen INSTANCE;

        private UtilityScreen() {
            super(Text.empty());
        }

        @Override
        public void renderTextHoverEffect(MatrixStack matrices, @Nullable Style style, int x, int y) {
            super.renderTextHoverEffect(matrices, style, x, y);
        }

        public static UtilityScreen get() {
            if (INSTANCE == null) {
                INSTANCE = new UtilityScreen();

                final var client = MinecraftClient.getInstance();
                INSTANCE.init(
                        client,
                        client.getWindow().getScaledWidth(),
                        client.getWindow().getScaledHeight()
                );
            }

            return INSTANCE;
        }

        static {
            WindowResizeCallback.EVENT.register((client, window) -> {
                if (INSTANCE == null) return;
                INSTANCE.init(client, window.getScaledWidth(), window.getScaledHeight());
            });
        }
    }

}
