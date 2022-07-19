package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.ScissorStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LabelComponent extends BaseComponent {

    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    protected Text text;
    protected List<OrderedText> wrappedText;

    protected int color;
    protected boolean shadow;
    // TODO get rid of this
    protected int maxWidth;

    protected LabelComponent(Text text) {
        this.text = text;
        this.wrappedText = new ArrayList<>();

        this.color = 0xFFFFFF;
        this.shadow = false;
        this.maxWidth = Integer.MAX_VALUE;

        this.wrapLines();
    }

    public LabelComponent text(Text text) {
        this.text = text;
        this.wrapLines();
        return this;
    }

    public Text text() {
        return this.text;
    }

    public LabelComponent maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        this.wrapLines();
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

    public LabelComponent color(int color) {
        this.color = color;
        return this;
    }

    public int color() {
        return this.color;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        int widestText = 0;
        for (var line : this.wrappedText) {
            int width = this.textRenderer.getWidth(line);
            if (width > widestText) widestText = width;
        }

        if (widestText > this.maxWidth) {
            this.wrapLines();
            this.applyHorizontalContentSizing(sizing);
        } else {
            this.width = widestText + sizing.value * 2;
        }
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.wrapLines();
        this.height = (this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2 + sizing.value * 2;
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);

        if (this.horizontalSizing.get().method != Sizing.Method.CONTENT) {
            this.maxWidth = width;
            this.wrapLines();
        }
    }

    private void wrapLines() {
        this.wrappedText = this.textRenderer.wrapLines(this.text, this.maxWidth);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        int x = this.x;
        int y = this.y;

        if (this.horizontalSizing.get().method == Sizing.Method.CONTENT) {
            x += this.horizontalSizing.get().value;
        }
        if (this.verticalSizing.get().method == Sizing.Method.CONTENT) {
            y += this.verticalSizing.get().value;
        }

        for (int i = 0; i < this.wrappedText.size(); i++) {
            if (this.shadow) {
                this.textRenderer.drawWithShadow(matrices, this.wrappedText.get(i), x, y + i * 11, this.color);
            } else {
                this.textRenderer.draw(matrices, this.wrappedText.get(i), x, y + i * 11, this.color);
            }
        }

        if (this.isInBoundingBox(mouseX, mouseY)) {
            ScissorStack.drawUnclipped(() -> EventScreen.get().renderTextHoverEffect(matrices, this.text.getStyle(), mouseX, mouseY));
        }
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return EventScreen.get().handleTextClick(this.text.getStyle()) | super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text", UIParsing::parseText, this::text);
        UIParsing.apply(children, "max-width", UIParsing::parseUnsignedInt, this::maxWidth);
        UIParsing.apply(children, "color", UIParsing::parseColor, this::color);
        UIParsing.apply(children, "shadow", UIParsing::parseBool, this::shadow);
    }

    protected static class EventScreen extends Screen {

        private static EventScreen instance;

        private EventScreen() {
            super(Text.empty());
        }

        @Override
        public void renderTextHoverEffect(MatrixStack matrices, @Nullable Style style, int x, int y) {
            super.renderTextHoverEffect(matrices, style, x, y);
        }

        public static EventScreen get() {
            if (instance == null) {
                instance = new EventScreen();
                instance.init(MinecraftClient.getInstance(), Integer.MAX_VALUE, Integer.MAX_VALUE);
            }

            return instance;
        }
    }
}
