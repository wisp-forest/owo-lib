package io.wispforest.owo.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Insets;
import io.wispforest.owo.ui.definitions.ParentComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

/**
 * An extension of vanilla's {@link DrawableHelper} with all methods
 * statically accessible as well as extra convenience methods
 */
public class Drawer extends DrawableHelper {

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
     * @param matrices   The transformation matrix stack
     * @param x          The x-coordinate of top-left corner of the rectangle
     * @param y          The y-coordinate of top-left corner of the rectangle
     * @param width      The width of the rectangle
     * @param height     The height of the rectangle
     * @param startColor The color at the rectangle's origin
     * @param endColor   The color at the corner opposite the origin
     */
    public static void drawGradientRect(MatrixStack matrices, int x, int y, int width, int height, int startColor, int endColor) {
        fillGradient(matrices, x, y, x + width, y + height, startColor, endColor, 0);
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
        RenderSystem.setShaderTexture(0, dark ? DARK_PANEL_TEXTURE : PANEL_TEXTURE);

        drawTexture(matrices, x, y, 0, 0, 5, 5, 16, 16);
        drawTexture(matrices, x + width - 5, y, 10, 0, 5, 5, 16, 16);
        drawTexture(matrices, x, y + height - 5, 0, 10, 5, 5, 16, 16);
        drawTexture(matrices, x + width - 5, y + height - 5, 10, 10, 5, 5, 16, 16);

        if (width > 10 && height > 10) {
            drawTexture(matrices, x + 5, y + 5, width - 10, height - 10, 5, 5, 5, 5, 16, 16);
        }

        if (width > 10) {
            drawTexture(matrices, x + 5, y, width - 10, 5, 5, 0, 5, 5, 16, 16);
            drawTexture(matrices, x + 5, y + height - 5, width - 10, 5, 5, 10, 5, 5, 16, 16);
        }

        if (height > 10) {
            drawTexture(matrices, x, y + 5, 5, height - 10, 0, 5, 5, 5, 16, 16);
            drawTexture(matrices, x + width - 5, y + 5, 5, height - 10, 10, 5, 5, 5, 16, 16);
        }
    }

    public static DebugDrawer debug() {
        return INSTANCE.debug;
    }

    public static class DebugDrawer {

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
            var textRenderer = MinecraftClient.getInstance().textRenderer;

            var children = new ArrayList<Component>();
            if (!onlyHovered) {
                root.collectChildren(children);
            } else if (root.childAt((int) mouseX, (int) mouseY) != null) {
                children.add(root.childAt((int) mouseX, (int) mouseY));
            }

            for (var child : children) {
                if (child instanceof ParentComponent layoutComponent) {
                    this.drawInsets(matrices, layoutComponent.x(), layoutComponent.y(), layoutComponent.width(),
                            layoutComponent.height(), layoutComponent.padding().get().inverted(), 0xA753BF9D);
                }

                final var margins = child.margins().get();
                this.drawInsets(matrices, child.x(), child.y(), child.width(), child.height(), margins, 0xA70000AF);
                drawRectOutline(matrices, child.x(), child.y(), child.width(), child.height(), 0xFF3AB0FF);

                textRenderer.draw(matrices, Text.of(child.getClass().getSimpleName() + (child.id() != null ? " '" + child.id() + "'" : "")),
                        child.x() + 1, child.y() + child.height() + 1, 0xFFFFFF);
                textRenderer.draw(matrices, Text.of(child.x() + "," + child.y() + " (" + child.width() + "," + child.height() + ")"
                                + "[" + margins.top() + "," + margins.bottom() + "," + margins.left() + "," + margins.right() + "]"),
                        child.x() + 1, child.y() + child.height() + textRenderer.fontHeight + 2, 0xFFFFFF);
            }
        }
    }

}
