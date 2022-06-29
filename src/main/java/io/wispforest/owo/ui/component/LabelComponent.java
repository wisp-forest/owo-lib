package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.BaseComponent;
import io.wispforest.owo.ui.definitions.Size;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.parse.OwoUIParsing;
import io.wispforest.owo.ui.parse.OwoUISpec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
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
    protected int maxWidth;

    public LabelComponent(Text text) {
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
    }

    @Override
    public void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        OwoUIParsing.apply(children, "text", OwoUIParsing::parseText, this::text);
        OwoUIParsing.apply(children, "max-width", OwoUIParsing::parseUnsignedInt, this::maxWidth);
        OwoUIParsing.apply(children, "color", OwoUIParsing::parseColor, this::color);
        OwoUIParsing.apply(children, "shadow", OwoUIParsing::parseBool, this::shadow);
    }
}
