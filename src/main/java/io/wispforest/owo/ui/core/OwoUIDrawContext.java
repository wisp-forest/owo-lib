package io.wispforest.owo.ui.core;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.mixin.ui.access.DrawContextAccessor;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.renderstate.CircleElementRenderState;
import io.wispforest.owo.ui.renderstate.GradientQuadElementRenderState;
import io.wispforest.owo.ui.renderstate.LineElementRenderState;
import io.wispforest.owo.ui.renderstate.RingElementRenderState;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OwoUIDrawContext extends DrawContext {

    public static final Identifier PANEL_NINE_PATCH_TEXTURE = Identifier.of("owo", "panel/default");
    public static final Identifier DARK_PANEL_NINE_PATCH_TEXTURE = Identifier.of("owo", "panel/dark");
    public static final Identifier PANEL_INSET_NINE_PATCH_TEXTURE = Identifier.of("owo", "panel/inset");

    private final Consumer<Runnable> setTooltipDrawer;

    private OwoUIDrawContext(MinecraftClient client, GuiRenderState renderState, Consumer<Runnable> setTooltipDrawer) {
        super(client, renderState);
        this.setTooltipDrawer = setTooltipDrawer;
    }

    public static OwoUIDrawContext of(DrawContext context) {
        var owoContext = new OwoUIDrawContext(
            MinecraftClient.getInstance(),
            context.state,
            ((DrawContextAccessor) context)::owo$setTooltipDrawer
        );

        ((DrawContextAccessor) owoContext).owo$setScissorStack(((DrawContextAccessor) context).owo$getScissorStack());
        ((DrawContextAccessor) owoContext).owo$setMatrices(((DrawContextAccessor) context).owo$getMatrices());

        return owoContext;
    }

    public static UtilityScreen utilityScreen() {
        return UtilityScreen.get();
    }

    public boolean intersectsScissor(PositionedRectangle other) {
        var rect = this.scissorStack.peekLast();

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
        this.state.addSimpleElement(new GradientQuadElementRenderState(
            pipeline,
            new Matrix3x2f(this.getMatrices()),
            new ScreenRect(new ScreenPos(x, y), width, height),
            this.scissorStack.peekLast(),
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
        this.state.addSimpleElement(new GradientQuadElementRenderState(
            OwoUIPipelines.GUI_HSV,
            new Matrix3x2f(this.getMatrices()),
            new ScreenRect(new ScreenPos(x, y), width, height),
            this.scissorStack.peekLast(),
            Color.WHITE,
            new Color(vertical ? 1f : 0f, 1f, 1f),
            new Color(vertical ? 0f : 1f, 1f, 1f),
            new Color(0f, 1f, 1f)
        ));
    }

    public void drawText(Text text, float x, float y, float scale, int color) {
        drawText(text, x, y, scale, color, TextAnchor.TOP_LEFT);
    }

    public void drawText(Text text, float x, float y, float scale, int color, TextAnchor anchorPoint) {
        drawText(text, x, y, scale, color, anchorPoint, false);
    }

    public void drawText(Text text, float x, float y, float scale, int color, TextAnchor anchorPoint, boolean shadow) {
        final var textRenderer = MinecraftClient.getInstance().textRenderer;

        this.getMatrices().pushMatrix();
        this.getMatrices().scale(scale, scale);

        switch (anchorPoint) {
            case TOP_RIGHT -> x -= textRenderer.getWidth(text) * scale;
            case BOTTOM_LEFT -> y -= textRenderer.fontHeight * scale;
            case BOTTOM_RIGHT -> {
                x -= textRenderer.getWidth(text) * scale;
                y -= textRenderer.fontHeight * scale;
            }
        }


        this.drawText(textRenderer, text, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color, shadow);
        this.getMatrices().popMatrix();
    }

    public enum TextAnchor {
        TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT
    }

    public void drawScrollableText(Text text, int x, int y, int width, int height, int horizontalMargin, int color, boolean centerText, boolean shadow) {
        int x1 = x + horizontalMargin;
        int x2 = x + width - horizontalMargin;
        
        drawScrollableText(text, x1, y, x2, y + height, color, centerText, shadow);
    }

    public void drawScrollableText(Text text, int startX, int startY, int endX, int endY, int color, boolean centerText, boolean shadow) {
        final var textRenderer = MinecraftClient.getInstance().textRenderer;
        
        int textWidth = textRenderer.getWidth(text);
        
        int totalHeight = startY + endY;
        int totalWidth = endX - startX;

        int centerY = (totalHeight - 9) / 2 + 1;
        
        if (textWidth > totalWidth) {
            int widthDelta = textWidth - totalWidth;

            double measuredSeconds = Util.getMeasuringTimeMs() / 1000.0D;
            
            double halfOrSoWidthDelta = Math.max(widthDelta * 0.5D, 3.0F);
            
            double delta = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * measuredSeconds / halfOrSoWidthDelta)) / 2.0D + 0.5D;

            double currentXOffset = MathHelper.lerp(delta, 0.0F, widthDelta);
            
            this.enableScissor(startX, startY, endX, endY);

            this.drawText(textRenderer, text, startX - (int)currentXOffset, centerY, color, shadow);

            this.disableScissor();
        } else if (centerText) {
            int centerX = (startX + endX) / 2;

            int y = MathHelper.clamp(centerX, startX + textWidth / 2, endX - textWidth / 2);

            this.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, shadow);
        } else {
            this.drawText(textRenderer, text, startX, startY, color, shadow);
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2, double thiccness, Color color) {
        drawLine(RenderPipelines.GUI, x1, y1, x2, y2, thiccness, color);
    }

    public void drawLine(RenderPipeline pipeline, int x1, int y1, int x2, int y2, double thiccness, Color color) {
        this.state.addSimpleElement(new LineElementRenderState(
            pipeline,
            new Matrix3x2f(this.getMatrices()),
            this.scissorStack.peekLast(),
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

        this.state.addSimpleElement(new CircleElementRenderState(
            pipeline,
            new Matrix3x2f(this.getMatrices()),
            this.scissorStack.peekLast(),
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

        this.state.addSimpleElement(new RingElementRenderState(
            pipeline,
            new Matrix3x2f(this.getMatrices()),
            this.scissorStack.peekLast(),
            centerX, centerY, angleFrom, angleTo, segments, innerRadius, outerRadius, innerColor, outerColor
        ));
    }

    public void drawTooltip(TextRenderer textRenderer, int x, int y, List<TooltipComponent> components) {
        drawTooltip(textRenderer, x, y, components, null);
    }

    public void drawTooltip(TextRenderer textRenderer, int x, int y, List<TooltipComponent> components, @Nullable Identifier texture) {
        ((DrawContextAccessor) this).owo$drawTooltipImmediately(textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE, texture);
    }

    @Override
    protected void drawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture, boolean focused) {
        super.drawTooltip(textRenderer, components, x, y, positioner, texture, focused);
        this.setTooltipDrawer.accept(((DrawContextAccessor) this).owo$getTooltipDrawer());
    }

    // --- debug rendering ---

    public void drawInsets(int x, int y, int width, int height, Insets insets, int color) {
        drawInsets(RenderPipelines.GUI, x, y, width, height, insets, color);
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
    public void drawInsets(RenderPipeline pipeline, int x, int y, int width, int height, Insets insets, int color) {
        this.fill(pipeline, x - insets.left(), y - insets.top(), x + width + insets.right(), y, color);
        this.fill(pipeline, x - insets.left(), y + height, x + width + insets.right(), y + height + insets.bottom(), color);

        this.fill(pipeline, x - insets.left(), y, x, y + height, color);
        this.fill(pipeline, x + width, y, x + width + insets.right(), y + height, color);
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
    public void drawInspector(ParentComponent root, double mouseX, double mouseY, boolean onlyHovered) {
        var client = MinecraftClient.getInstance();
        var textRenderer = client.textRenderer;

        var children = new ArrayList<Component>();
        if (!onlyHovered) {
            root.collectDescendants(children);
        } else if (root.childAt((int) mouseX, (int) mouseY) != null) {
            children.add(root.childAt((int) mouseX, (int) mouseY));
        }

        var pipeline = RenderPipelines.GUI;

        for (var child : children) {
            if (child instanceof ParentComponent parentComponent) {
                this.drawInsets(pipeline, parentComponent.x(), parentComponent.y(), parentComponent.width(),
                    parentComponent.height(), parentComponent.padding().get().inverted(), 0xA70CECDD);
            }

            final var margins = child.margins().get();
            this.drawInsets(pipeline, child.x(), child.y(), child.width(), child.height(), margins, 0xA7FFF338);
            this.drawRectOutline(pipeline, child.x(), child.y(), child.width(), child.height(), 0xFF3AB0FF);

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
                this.fill(pipeline, inspectorX, inspectorY, inspectorX + width + 3, inspectorY + inspectorHeight, 0xA7000000);
                this.drawRectOutline(pipeline, inspectorX, inspectorY, width + 3, inspectorHeight, 0xA7000000);

                this.drawText(textRenderer, nameText, inspectorX + 2, inspectorY + 2, 0xFFFFFFFF, false);
                this.drawText(textRenderer, descriptor, inspectorX + 2, inspectorY + textRenderer.fontHeight + 2, 0xFFFFFFFF, false);
            }
        }
    }

    public static class UtilityScreen extends Screen {

        private static UtilityScreen INSTANCE;

        private Screen linkSourceScreen = null;

        private UtilityScreen() {
            super(Text.empty());
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

        /**
         * Set the screen to which the game should return after the {@link net.minecraft.client.gui.screen.ConfirmLinkScreen}
         * opened by {@link #handleTextClick(Style)} to {@code screen}
         *
         * @see #handleTextClick(Style)
         */
        public void setLinkSource(Screen screen) {
            this.linkSourceScreen = screen;
        }

        /**
         * Invoke {@link #setLinkSource(Screen)} with the current screen. Used by the default text click handler
         * in {@link io.wispforest.owo.ui.component.LabelComponent}
         */
        public void captureLinkSource() {
            this.setLinkSource(this.client.currentScreen);
        }

        @ApiStatus.Internal
        public @Nullable Screen getAndClearLinkSource() {
            var source = this.linkSourceScreen;
            this.linkSourceScreen = null;

            return source;
        }

        /**
         * Since the vanilla implementation of this method always returns to the screen the method was invoked on
         * (which, here, would be the utility screen which is not what we want), either {@link #captureLinkSource()}
         * or {@link #setLinkSource(Screen)} must be called prior to invoking this method
         */
        @Override
        public boolean handleTextClick(@Nullable Style style) {
            return super.handleTextClick(style);
        }

        static {
            WindowResizeCallback.EVENT.register((client, window) -> {
                if (INSTANCE == null) return;
                INSTANCE.init(client, window.getScaledWidth(), window.getScaledHeight());
            });
        }
    }
}
