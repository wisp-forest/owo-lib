package io.wispforest.owo.ui.core;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.mixin.ui.access.GuiGraphicsAccessor;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.renderstate.CircleElementRenderState;
import io.wispforest.owo.ui.renderstate.GradientQuadElementRenderState;
import io.wispforest.owo.ui.renderstate.LineElementRenderState;
import io.wispforest.owo.ui.renderstate.RingElementRenderState;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OwoUIGraphics extends GuiGraphics {

    public static final Identifier PANEL_NINE_PATCH_TEXTURE = Identifier.fromNamespaceAndPath("owo", "panel/default");
    public static final Identifier DARK_PANEL_NINE_PATCH_TEXTURE = Identifier.fromNamespaceAndPath("owo", "panel/dark");
    public static final Identifier PANEL_INSET_NINE_PATCH_TEXTURE = Identifier.fromNamespaceAndPath("owo", "panel/inset");

    private final Consumer<Runnable> setTooltipDrawer;

    protected OwoUIGraphics(Minecraft client, GuiRenderState renderState, int mouseX, int mouseY, Consumer<Runnable> setTooltipDrawer) {
        super(client, renderState, mouseX, mouseY);
        this.setTooltipDrawer = setTooltipDrawer;
    }

    public static OwoUIGraphics of(GuiGraphics graphics) {
        var owoContext = new OwoUIGraphics(
            Minecraft.getInstance(),
            graphics.guiRenderState,
            ((GuiGraphicsAccessor) graphics).owo$getMouseY(),
            ((GuiGraphicsAccessor) graphics).owo$getMouseX(),
            ((GuiGraphicsAccessor) graphics)::owo$setDeferredTooltip
        );

        ((GuiGraphicsAccessor) owoContext).owo$setScissorStack(((GuiGraphicsAccessor) graphics).owo$getScissorStack());
        ((GuiGraphicsAccessor) owoContext).owo$setPose(((GuiGraphicsAccessor) graphics).owo$getPose());

        return owoContext;
    }

    public static UtilityScreen utilityScreen() {
        return UtilityScreen.get();
    }

    public boolean intersectsScissor(PositionedRectangle other) {
        other = other.transform(getMatrixStack());

        var rect = this.scissorStack.peek();

        if (rect == null) return true;

        var pos = rect.position();

        return other.x() < pos.x() + rect.width()
            && other.x() + other.width() >= pos.x()
            && other.y() < pos.y() + rect.height()
            && other.y() + other.height() >= pos.y();
    }

    public void drawRectOutline(int x, int y, int width, int height, int color) {
        drawRectOutline(RenderPipelines.GUI, x, y, width, height, color);
    }

    /**
     * Draw the outline of a rectangle
     *
     * @param x      The x-coordinate of top-left corner of the rectangle
     * @param y      The y-coordinate of top-left corner of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param color  The color of the rectangle
     */
    public void drawRectOutline(RenderPipeline pipeline, int x, int y, int width, int height, int color) {
        this.fill(pipeline, x, y, x + width, y + 1, color);
        this.fill(pipeline, x, y + height - 1, x + width, y + height, color);

        this.fill(pipeline, x, y + 1, x + 1, y + height - 1, color);
        this.fill(pipeline, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public void drawGradientRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
        this.drawGradientRect(RenderPipelines.GUI, x, y, width, height, topLeftColor, topRightColor, bottomRightColor, bottomLeftColor);
    }

    /**
     * Draw a filled rectangle with a gradient
     *
     * @param x                The x-coordinate of top-left corner of the rectangle
     * @param y                The y-coordinate of top-left corner of the rectangle
     * @param width            The width of the rectangle
     * @param height           The height of the rectangle
     * @param topLeftColor     The color at the rectangle's top left corner
     * @param topRightColor    The color at the rectangle's top right corner
     * @param bottomRightColor The color at the rectangle's bottom right corner
     * @param bottomLeftColor  The color at the rectangle's bottom left corner
     */
    public void drawGradientRect(RenderPipeline pipeline, int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
        this.guiRenderState.submitGuiElement(new GradientQuadElementRenderState(
            pipeline,
            new Matrix3x2f(this.pose()),
            new ScreenRectangle(new ScreenPosition(x, y), width, height),
            this.scissorStack.peek(),
            Color.ofArgb(topLeftColor),
            Color.ofArgb(topRightColor),
            Color.ofArgb(bottomLeftColor),
            Color.ofArgb(bottomRightColor)
        ));
    }

    /**
     * Draw a panel that looks like the background of a vanilla
     * inventory screen
     *
     * @param x      The x-coordinate of top-left corner of the panel
     * @param y      The y-coordinate of top-left corner of the panel
     * @param width  The width of the panel
     * @param height The height of the panel
     * @param dark   Whether to use the dark version of the panel texture
     */
    public void drawPanel(int x, int y, int width, int height, boolean dark) {
        NinePatchTexture.draw(dark ? DARK_PANEL_NINE_PATCH_TEXTURE : PANEL_NINE_PATCH_TEXTURE, this, x, y, width, height);
    }

    public void drawSpectrum(int x, int y, int width, int height, boolean vertical) {
        this.guiRenderState.submitGuiElement(new GradientQuadElementRenderState(
            OwoUIPipelines.GUI_HSV,
            new Matrix3x2f(this.pose()),
            new ScreenRectangle(new ScreenPosition(x, y), width, height),
            this.scissorStack.peek(),
            Color.WHITE,
            new Color(vertical ? 1f : 0f, 1f, 1f),
            new Color(vertical ? 0f : 1f, 1f, 1f),
            new Color(0f, 1f, 1f)
        ));
    }

    public void drawText(Component text, float x, float y, float scale, int color) {
        drawText(text, x, y, scale, color, TextAnchor.TOP_LEFT);
    }

    public void drawText(Component text, float x, float y, float scale, int color, TextAnchor anchorPoint) {
        final var textRenderer = Minecraft.getInstance().font;

        this.pose().pushMatrix();
        this.pose().scale(scale, scale);

        switch (anchorPoint) {
            case TOP_RIGHT -> x -= textRenderer.width(text) * scale;
            case BOTTOM_LEFT -> y -= textRenderer.lineHeight * scale;
            case BOTTOM_RIGHT -> {
                x -= textRenderer.width(text) * scale;
                y -= textRenderer.lineHeight * scale;
            }
        }


        this.drawString(textRenderer, text, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color, false);
        this.pose().popMatrix();
    }

    public enum TextAnchor {
        TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT
    }

    public void drawLine(int x1, int y1, int x2, int y2, double thiccness, Color color) {
        drawLine(RenderPipelines.GUI, x1, y1, x2, y2, thiccness, color);
    }

    public void drawLine(RenderPipeline pipeline, int x1, int y1, int x2, int y2, double thiccness, Color color) {
        this.guiRenderState.submitGuiElement(new LineElementRenderState(
            pipeline,
            new Matrix3x2f(this.pose()),
            this.scissorStack.peek(),
            x1, y1, x2, y2,
            thiccness,
            color
        ));
    }

    public void drawCircle(int centerX, int centerY, int segments, double radius, Color color) {
        drawCircle(OwoUIPipelines.GUI_TRIANGLE_FAN, centerX, centerY, segments, radius, color);
    }

    public void drawCircle(int centerX, int centerY, double angleFrom, double angleTo, int segments, double radius, Color color) {
        drawCircle(OwoUIPipelines.GUI_TRIANGLE_FAN, centerX, centerY, angleFrom, angleTo, segments, radius, color);
    }

    public void drawCircle(RenderPipeline pipeline, int centerX, int centerY, int segments, double radius, Color color) {
        drawCircle(pipeline, centerX, centerY, 0, 360, segments, radius, color);
    }

    public void drawCircle(RenderPipeline pipeline, int centerX, int centerY, double angleFrom, double angleTo, int segments, double radius, Color color) {
        Preconditions.checkArgument(angleFrom < angleTo, "angleFrom must be less than angleTo");

        this.guiRenderState.submitGuiElement(new CircleElementRenderState(
            pipeline,
            new Matrix3x2f(this.pose()),
            this.scissorStack.peek(),
            centerX, centerY, angleFrom, angleTo, segments, radius, color
        ));
    }

    public void drawRing(int centerX, int centerY, int segments, double innerRadius, double outerRadius, Color innerColor, Color outerColor) {
        drawRing(OwoUIPipelines.GUI_TRIANGLE_STRIP, centerX, centerY, segments, innerRadius, outerRadius, innerColor, outerColor);
    }

    public void drawRing(int centerX, int centerY, double angleFrom, double angleTo, int segments, double innerRadius, double outerRadius, Color innerColor, Color outerColor) {
        drawRing(OwoUIPipelines.GUI_TRIANGLE_STRIP, centerX, centerY, angleFrom, angleTo, segments, innerRadius, outerRadius, innerColor, outerColor);
    }

    public void drawRing(RenderPipeline pipeline, int centerX, int centerY, int segments, double innerRadius, double outerRadius, Color innerColor, Color outerColor) {
        drawRing(pipeline, centerX, centerY, 0d, 360d, segments, innerRadius, outerRadius, innerColor, outerColor);
    }

    public void drawRing(RenderPipeline pipeline, int centerX, int centerY, double angleFrom, double angleTo, int segments, double innerRadius, double outerRadius, Color innerColor, Color outerColor) {
        Preconditions.checkArgument(angleFrom < angleTo, "angleFrom must be less than angleTo");
        Preconditions.checkArgument(innerRadius < outerRadius, "innerRadius must be less than outerRadius");

        this.guiRenderState.submitGuiElement(new RingElementRenderState(
            pipeline,
            new Matrix3x2f(this.pose()),
            this.scissorStack.peek(),
            centerX, centerY, angleFrom, angleTo, segments, innerRadius, outerRadius, innerColor, outerColor
        ));
    }

    public void drawTooltip(Font textRenderer, int x, int y, List<ClientTooltipComponent> components) {
        drawTooltip(textRenderer, x, y, components, null);
    }

    public void drawTooltip(Font textRenderer, int x, int y, List<ClientTooltipComponent> components, @Nullable Identifier texture) {
        ((GuiGraphicsAccessor) this).owo$drawTooltipImmediately(textRenderer, components, x, y, DefaultTooltipPositioner.INSTANCE, texture);
    }

    @Override
    protected void setTooltipForNextFrameInternal(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier texture, boolean focused) {
        super.setTooltipForNextFrameInternal(textRenderer, components, x, y, positioner, texture, focused);
        this.setTooltipDrawer.accept(((GuiGraphicsAccessor) this).owo$getDeferredTooltip());
    }

    // --- debug rendering ---

    public static void drawInsets(OwoUIGraphics self, int x, int y, int width, int height, Insets insets, int color) {
        drawInsets(self, RenderPipelines.GUI, x, y, width, height, insets, color);
    }

    /**
     * Draw the area around the given rectangle which
     * the given insets describe
     *
     * @param x      The x-coordinate of top-left corner of the rectangle
     * @param y      The y-coordinate of top-left corner of the rectangle
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param insets The insets to draw around the rectangle
     * @param color  The color to draw the inset area with
     */
    public static void drawInsets(OwoUIGraphics self, RenderPipeline pipeline, int x, int y, int width, int height, Insets insets, int color) {
        self.fill(pipeline, x - insets.left(), y - insets.top(), x + width + insets.right(), y, color);
        self.fill(pipeline, x - insets.left(), y + height, x + width + insets.right(), y + height + insets.bottom(), color);

        self.fill(pipeline, x - insets.left(), y, x, y + height, color);
        self.fill(pipeline, x + width, y, x + width + insets.right(), y + height, color);
    }

    /**
     * Draw the element inspector for the given tree, detailing the position,
     * bounding box, margins and padding of each component
     *
     * @param root        The root component of the hierarchy to draw
     * @param mouseX      The x-coordinate of the mouse pointer
     * @param mouseY      The y-coordinate of the mouse pointer
     * @param onlyHovered Whether to only draw the inspector for the hovered widget
     */
    public static void drawInspector(OwoUIGraphics self, ParentUIComponent root, double mouseX, double mouseY, boolean onlyHovered) {
        var client = Minecraft.getInstance();
        var textRenderer = client.font;

        var children = new ArrayList<UIComponent>();
        if (!onlyHovered) {
            root.collectDescendants(children);
        } else if (root.childAt((int) mouseX, (int) mouseY) != null) {
            children.add(root.childAt((int) mouseX, (int) mouseY));
        }

        var pipeline = RenderPipelines.GUI;

        for (var child : children) {
            if (child instanceof ParentUIComponent parentComponent) {
                drawInsets(self, pipeline, parentComponent.x(), parentComponent.y(), parentComponent.width(),
                    parentComponent.height(), parentComponent.padding().get().inverted(), 0xA70CECDD);
            }

            final var margins = child.margins().get();
            drawInsets(self, pipeline, child.x(), child.y(), child.width(), child.height(), margins, 0xA7FFF338);
            self.drawRectOutline(pipeline, child.x(), child.y(), child.width(), child.height(), 0xFF3AB0FF);

            if (onlyHovered) {

                int inspectorX = child.x() + 1;
                int inspectorY = child.y() + child.height() + child.margins().get().bottom() + 1;

                final var message = Component.literal(child.getClass().getSimpleName())
                    .append(child.id() == null ? "\n" : " '" + child.id() + "'\n")
                    .append(child.inspectorDescriptor());
                final var wrappedMessage = textRenderer.split(message, client.getWindow().getGuiScaledWidth() + 4);
                int inspectorWidth = wrappedMessage.stream().mapToInt(textRenderer::width).max().orElse(30);
                int inspectorHeight = textRenderer.lineHeight * wrappedMessage.size() + 4;

                if (inspectorY > client.getWindow().getGuiScaledHeight() - inspectorHeight) {
                    inspectorY -= child.fullSize().height() + inspectorHeight + 1;
                    if (child instanceof ParentUIComponent parentComponent) {
                        inspectorX += parentComponent.padding().get().left();
                        inspectorY += parentComponent.padding().get().top();
                    }
                }
                if (inspectorY < 0) inspectorY = 1;

                if (inspectorX > client.getWindow().getGuiScaledWidth() - inspectorWidth) {
                    inspectorX = client.getWindow().getGuiScaledWidth() - inspectorWidth - 2;
                }
                if (inspectorX < 0) inspectorX = 1;

                self.fill(pipeline, inspectorX, inspectorY, inspectorX + inspectorWidth + 3, inspectorY + inspectorHeight, 0xA7000000);
                self.drawRectOutline(pipeline, inspectorX, inspectorY, inspectorWidth + 3, inspectorHeight, 0xA7000000);

                self.drawWordWrap(textRenderer, message, inspectorX + 2, inspectorY + 2, inspectorWidth, 0xFFFFFFFF, false);
            }
        }
    }

    public static class UtilityScreen extends Screen {

        private static UtilityScreen INSTANCE;

        private UtilityScreen() {
            super(Component.empty());
        }

        public static UtilityScreen get() {
            if (INSTANCE == null) {
                INSTANCE = new UtilityScreen();

                final var client = Minecraft.getInstance();
                INSTANCE.init(
                    client.getWindow().getGuiScaledWidth(),
                    client.getWindow().getGuiScaledHeight()
                );
            }

            return INSTANCE;
        }

        public boolean handleTextClick(Style style, Screen screenAfterRun) {
            if (style.getClickEvent() == null) return false;
            defaultHandleGameClickEvent(style.getClickEvent(), this.minecraft, screenAfterRun);

            return true;
        }

        static {
            WindowResizeCallback.EVENT.register((client, window) -> {
                if (INSTANCE == null) return;
                INSTANCE.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            });
        }
    }
}
