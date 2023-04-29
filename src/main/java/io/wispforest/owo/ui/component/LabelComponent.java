package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.util.pond.OwoTextRendererExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LabelComponent extends BaseComponent {

    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    protected Text text;
    protected List<OrderedText> wrappedText;

    protected VerticalAlignment verticalTextAlignment = VerticalAlignment.TOP;
    protected HorizontalAlignment horizontalTextAlignment = HorizontalAlignment.LEFT;

    protected final AnimatableProperty<Color> color = AnimatableProperty.of(Color.WHITE);
    protected boolean shadow;
    protected int maxWidth;

    protected LabelComponent(Text text) {
        this.text = text;
        this.wrappedText = new ArrayList<>();

        this.shadow = false;
        this.maxWidth = Integer.MAX_VALUE;
    }

    public LabelComponent text(Text text) {
        this.text = text;
        this.notifyParentIfMounted();
        return this;
    }

    public Text text() {
        return this.text;
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
        return (this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2;
    }

    @Override
    public void inflate(Size space) {
        this.wrapLines();
        super.inflate(space);
    }

    private void wrapLines() {
        this.wrappedText = this.textRenderer.wrapLines(this.text, this.horizontalSizing.get().isContent() ? this.maxWidth : this.width);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.color.update(delta);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        try {
            int x = this.x;
            int y = this.y;

            if (this.horizontalSizing.get().isContent()) {
                x += this.horizontalSizing.get().value;
            }
            if (this.verticalSizing.get().isContent()) {
                y += this.verticalSizing.get().value;
            }

            switch (this.verticalTextAlignment) {
                case CENTER -> y += (this.height - ((this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2)) / 2;
                case BOTTOM -> y += this.height - ((this.wrappedText.size() * (this.textRenderer.fontHeight + 2)) - 2);
            }

            ((OwoTextRendererExtension) this.textRenderer).owo$beginCache();

            for (int i = 0; i < this.wrappedText.size(); i++) {
                var renderText = this.wrappedText.get(i);
                int renderX = x;

                switch (this.horizontalTextAlignment) {
                    case CENTER -> renderX += (this.width - this.textRenderer.getWidth(renderText)) / 2;
                    case RIGHT -> renderX += this.width - this.textRenderer.getWidth(renderText);
                }

                if (this.shadow) {
                    this.textRenderer.drawWithShadow(matrices, renderText, renderX, y + i * 11, this.color.get().argb());
                } else {
                    this.textRenderer.draw(matrices, renderText, renderX, y + i * 11, this.color.get().argb());
                }
            }
        } finally {
            ((OwoTextRendererExtension) this.textRenderer).owo$submitCache();
        }
    }

    @Override
    public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.drawTooltip(matrices, mouseX, mouseY, partialTicks, delta);

        if (!this.isInBoundingBox(mouseX, mouseY)) return;
        Drawer.utilityScreen().renderTextHoverEffect(matrices, this.styleAt(mouseX - this.x, mouseY - this.y), mouseX, mouseY);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return Drawer.utilityScreen().handleTextClick(this.styleAt((int) mouseX, (int) mouseY)) | super.onMouseDown(mouseX, mouseY, button);
    }

    private Style styleAt(int mouseX, int mouseY) {
        return this.textRenderer.getTextHandler().getStyleAt(this.wrappedText.get(Math.min(mouseY / 11, this.wrappedText.size() - 1)), mouseX);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text", UIParsing::parseText, this::text);
        UIParsing.apply(children, "max-width", UIParsing::parseUnsignedInt, this::maxWidth);
        UIParsing.apply(children, "color", Color::parse, this::color);
        UIParsing.apply(children, "shadow", UIParsing::parseBool, this::shadow);

        UIParsing.apply(children, "vertical-text-alignment", VerticalAlignment::parse, this::verticalTextAlignment);
        UIParsing.apply(children, "horizontal-text-alignment", HorizontalAlignment::parse, this::horizontalTextAlignment);
    }
}
