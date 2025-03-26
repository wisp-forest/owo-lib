package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.MouseEnter;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.ScissorStack;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LabelComponent extends BaseComponent {

    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    protected Text text;
    protected List<OrderedText> wrappedText;

    protected VerticalAlignment verticalTextAlignment = VerticalAlignment.TOP;
    protected HorizontalAlignment horizontalTextAlignment = HorizontalAlignment.LEFT;

    protected final AnimatableProperty<Color> color = AnimatableProperty.of(Color.WHITE);

    @Nullable
    protected Text hoverText = null;
    @Nullable
    protected AnimatableProperty<Color> hoverColor = null;

    protected final Observable<Integer> lineHeight = Observable.of(this.textRenderer.fontHeight);
    protected final Observable<Integer> lineSpacing = Observable.of(2);
    protected boolean shadow;
    protected int maxWidth;

    protected boolean scrolling = false;

    protected Function<Style, Boolean> textClickHandler = style -> {
        OwoUIDrawContext.utilityScreen().captureLinkSource();
        var success = OwoUIDrawContext.utilityScreen().handleTextClick(style);
        OwoUIDrawContext.utilityScreen().getAndClearLinkSource();

        return success;
    };

    protected LabelComponent(Text text) {
        this.text = text;
        this.wrappedText = new ArrayList<>();

        this.shadow = false;
        this.maxWidth = Integer.MAX_VALUE;

        Observable.observeAll(this::notifyParentIfMounted, this.lineHeight, this.lineSpacing);
    }

    public LabelComponent text(Text text) {
        this.text = text;
        this.notifyParentIfMounted();
        return this;
    }

    public Text text() {
        return this.text;
    }

    public LabelComponent hoverText(@Nullable Text text) {
        if (this.text.equals(text)) {
            this.hoverText = null;
        } else {
            this.hoverText = text;
        }

        if (this.hoverText != null && this.hovered) {
            this.notifyParentIfMounted();
        }

        return this;
    }

    public Text hoverText() {
        return this.hoverText;
    }

    public Text currentText() {
        return this.hoverText != null && this.hovered ? this.hoverText : this.text;
    }

    public LabelComponent maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        this.notifyParentIfMounted();
        return this;
    }

    public int maxWidth() {
        return this.maxWidth;
    }

    public LabelComponent shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public boolean shadow() {
        return this.shadow;
    }

    public LabelComponent color(Color color) {
        this.color.set(color);
        return this;
    }

    public AnimatableProperty<Color> color() {
        return this.color;
    }

    public Color currentColor() {
        return this.hoverColor != null && this.hovered ? this.hoverColor.get() : this.color.get();
    }

    public LabelComponent hoverColor(Color hoverColor) {
        if (this.hoverColor == null) {
            this.hoverColor = AnimatableProperty.of(hoverColor);
        } else {
            this.hoverColor.set(hoverColor);
        }

        return this;
    }

    public AnimatableProperty<Color> hoverColor() {
        return this.hoverColor;
    }

    public LabelComponent verticalTextAlignment(VerticalAlignment verticalAlignment) {
        this.verticalTextAlignment = verticalAlignment;
        return this;
    }

    public VerticalAlignment verticalTextAlignment() {
        return this.verticalTextAlignment;
    }

    public LabelComponent horizontalTextAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalTextAlignment = horizontalAlignment;
        return this;
    }

    public HorizontalAlignment horizontalTextAlignment() {
        return this.horizontalTextAlignment;
    }

    public LabelComponent lineHeight(int lineHeight) {
        this.lineHeight.set(lineHeight);
        return this;
    }

    public int lineHeight() {
        return this.lineHeight.get();
    }

    public LabelComponent lineSpacing(int lineSpacing) {
        this.lineSpacing.set(lineSpacing);
        return this;
    }

    public int lineSpacing() {
        return this.lineSpacing.get();
    }

    public LabelComponent textClickHandler(Function<Style, Boolean> textClickHandler) {
        this.textClickHandler = textClickHandler;
        return this;
    }

    public Function<Style, Boolean> textClickHandler() {
        return textClickHandler;
    }

    public LabelComponent scrolling(boolean value) {
        this.scrolling = value;

        return this;
    }

    public boolean scrolling() {
        return this.scrolling;
    }

    @Override
    protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
        super.updateHoveredState(mouseX, mouseY, nowHovered);

        if (this.hoverText != null) this.notifyParentIfMounted();
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        int widestText = 0;
        for (var line : this.wrappedText) {
            int width = this.textRenderer.getWidth(line);
            if (width > widestText) widestText = width;
        }

        if (widestText > this.maxWidth) {
            this.wrapLines();
            return this.determineHorizontalContentSize(sizing);
        } else {
            return widestText;
        }
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        this.wrapLines();
        return this.textHeight();
    }

    @Override
    public void inflate(Size space) {
        this.wrapLines();
        super.inflate(space);
    }

    private void wrapLines() {
        int width;

        if (scrolling) {
            width = Integer.MAX_VALUE;
        } else if(this.horizontalSizing.get().isContent()) {
            width = this.maxWidth;
        } else {
            width = this.width;
        }

        this.wrappedText = this.textRenderer.wrapLines(this.currentText(), width);
    }

    protected int textHeight() {
        return (this.wrappedText.size() * (this.lineHeight() + this.lineSpacing())) - this.lineSpacing();
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.color.update(delta);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        context.push()
                .translate(0, 1 / MinecraftClient.getInstance().getWindow().getScaleFactor());

        int x = this.x;
        int y = this.y;

        if (this.horizontalSizing.get().isContent()) {
            x += this.horizontalSizing.get().value;
        }
        if (this.verticalSizing.get().isContent()) {
            y += this.verticalSizing.get().value;
        }

        switch (this.verticalTextAlignment) {
            case CENTER -> y += (this.height - (this.textHeight())) / 2;
            case BOTTOM -> y += this.height - (this.textHeight());
        }

        var color = currentColor();

        if (this.scrolling) {
            drawScrollableText(context, delta, x, y, color);
        } else {
            drawWrappedText(context, x, y, color);
        }

        context.pop();
    }

    protected void drawWrappedText(DrawContext context, int x, int y, Color color) {
        for (int i = 0; i < this.wrappedText.size(); i++) {
            var renderText = this.wrappedText.get(i);
            int renderX = x;

            switch (this.horizontalTextAlignment) {
                case CENTER -> renderX += (this.width - this.textRenderer.getWidth(renderText)) / 2;
                case RIGHT -> renderX += this.width - this.textRenderer.getWidth(renderText);
            }

            int renderY = y + i * (this.lineHeight() + this.lineSpacing());
            renderY += this.lineHeight() - this.textRenderer.fontHeight;

            context.drawText(this.textRenderer, renderText, renderX, renderY, color.argb(), this.shadow);
        }

        context.draw();
    }

    public LabelComponent copyScrollData(LabelComponent component) {
        this.offsetTotal = component.offsetTotal;
        this.prevDirection = component.prevDirection;
        this.currentDirection = component.currentDirection;
        this.pausedTimeTotal = component.pausedTimeTotal;

        return this;
    }

    private float offsetTotal = 0;

    private Direction prevDirection = Direction.FORWARDS;
    private Direction currentDirection = Direction.FORWARDS;

    private float pausedTimeTotal = 0;

    protected void drawScrollableText(OwoUIDrawContext context, float delta, int startX, int startY, Color color) {
        int textWidth = textRenderer.getWidth(text);

        int j = (startY + startY + this.height() - 9) / 2 + 1;

        if (textWidth > this.width()) {
            int scrollAmount = textWidth - this.width();

            // 0 -> total / 2 : total / 2 -> total
            //        -1      :        1
            var baseRangeValue = (offsetTotal - (scrollAmount / 2f)) / (scrollAmount / 2f);

            var offset = delta * ((baseRangeValue * baseRangeValue) / -1.1f + 1);

            switch (currentDirection) {
                case FORWARDS -> {
                    if (offsetTotal + offset >= scrollAmount) {
                        currentDirection = Direction.PAUSED;
                        prevDirection = Direction.FORWARDS;

                        offsetTotal = scrollAmount;
                    } else {
                        offsetTotal += offset;
                    }
                }
                case BACKWARDS -> {
                    if (offsetTotal - offset <= 0) {
                        currentDirection = Direction.PAUSED;
                        prevDirection = Direction.BACKWARDS;

                        offsetTotal = 0;
                    } else {
                        offsetTotal -= offset;
                    }
                }
                case PAUSED -> {
                    if (pausedTimeTotal > (4 * 20)) {
                        currentDirection = switch (prevDirection) {
                            case FORWARDS -> Direction.BACKWARDS;
                            case BACKWARDS, PAUSED -> Direction.FORWARDS;
                        };

                        pausedTimeTotal = 0;
                    } else {
                        pausedTimeTotal += delta;
                    }
                }
            }

            context.drawWithScissor(startX, startY, this.width(), this.height(), ctx -> {
                ctx.drawText(textRenderer, this.currentText(), startX - Math.round(offsetTotal)/*- (int)scrolledOffset*/, y, color.argb(), this.shadow);
            });
        } else {
            var offset = switch (this.horizontalTextAlignment) {
                case CENTER -> (this.width - this.textRenderer.getWidth(this.currentText())) / 2;
                case RIGHT -> this.width - this.textRenderer.getWidth(this.currentText());
                case null, default -> 0;
            };

            context.drawText(textRenderer, this.currentText(), startX + offset, startY, color.argb(), this.shadow);
        }
    }

    private enum Direction {
        FORWARDS,
        BACKWARDS,
        PAUSED;
    }

    @Override
    public void drawTooltip(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
        context.drawHoverEvent(this.textRenderer, this.styleAt(mouseX - this.x, mouseY - this.y), mouseX, mouseY);
    }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        var hoveredStyle = this.styleAt((int) (mouseX - this.x), (int) (mouseY - this.y));
        return super.shouldDrawTooltip(mouseX, mouseY) || (hoveredStyle != null && hoveredStyle.getHoverEvent() != null && this.isInBoundingBox(mouseX, mouseY));
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return this.textClickHandler.apply(this.styleAt((int) mouseX, (int) mouseY)) | super.onMouseDown(mouseX, mouseY, button);
    }

    protected Style styleAt(int mouseX, int mouseY) {
        return this.textRenderer.getTextHandler().getStyleAt(this.wrappedText.get(Math.min(mouseY / (this.lineHeight() + this.lineSpacing()), this.wrappedText.size() - 1)), mouseX);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text", UIParsing::parseText, this::text);
        UIParsing.apply(children, "hover-text", UIParsing::parseText, this::hoverText);
        UIParsing.apply(children, "max-width", UIParsing::parseUnsignedInt, this::maxWidth);
        UIParsing.apply(children, "color", Color::parse, this::color);
        UIParsing.apply(children, "hover-color", Color::parse, this::hoverColor);
        UIParsing.apply(children, "shadow", UIParsing::parseBool, this::shadow);
        UIParsing.apply(children, "line-height", UIParsing::parseUnsignedInt, this::lineHeight);
        UIParsing.apply(children, "line-spacing", UIParsing::parseUnsignedInt, this::lineSpacing);
        UIParsing.apply(children, "scrolling", UIParsing::parseBool, this::scrolling);

        UIParsing.apply(children, "vertical-text-alignment", VerticalAlignment::parse, this::verticalTextAlignment);
        UIParsing.apply(children, "horizontal-text-alignment", HorizontalAlignment::parse, this::horizontalTextAlignment);
    }
}
