package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;

import java.util.Map;

public class BoxComponent extends BaseComponent {

    protected boolean fill = false;
    protected GradientDirection direction = GradientDirection.TOP_TO_BOTTOM;

    protected int startColor = 0xFF000000;
    protected int endColor = 0xFF000000;

    public BoxComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this.sizing(horizontalSizing, verticalSizing);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.fill) {
            switch (this.direction) {
                case TOP_TO_BOTTOM -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        this.startColor, this.startColor, this.endColor, this.endColor);
                case RIGHT_TO_LEFT -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        this.endColor, this.startColor, this.startColor, this.endColor);
                case BOTTOM_TO_TOP -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        this.endColor, this.endColor, this.startColor, this.startColor);
                case LEFT_TO_RIGHT -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        this.startColor, this.endColor, this.endColor, this.startColor);
            }
        } else {
            Drawer.drawRectOutline(matrices, this.x, this.y, this.width, this.height, this.startColor);
        }
    }

    public BoxComponent fill(boolean fill) {
        this.fill = fill;
        return this;
    }

    public boolean fill() {
        return this.fill;
    }

    public BoxComponent direction(GradientDirection direction) {
        this.direction = direction;
        return this;
    }

    public GradientDirection direction() {
        return this.direction;
    }

    public BoxComponent color(int color) {
        this.startColor = color;
        this.endColor = color;
        return this;
    }

    public BoxComponent startColor(int startColor) {
        this.startColor = startColor;
        return this;
    }

    public int startColor() {
        return this.startColor;
    }

    public BoxComponent endColor(int endColor) {
        this.endColor = endColor;
        return this;
    }

    public int endColor() {
        return this.endColor;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.expectChildren(element, children, "sizing");

        UIParsing.apply(children, "color", UIParsing::parseColor, this::color);
        UIParsing.apply(children, "start-color", UIParsing::parseColor, this::startColor);
        UIParsing.apply(children, "end-color", UIParsing::parseColor, this::endColor);
        UIParsing.apply(children, "fill", UIParsing::parseBool, this::fill);
        UIParsing.apply(children, "direction", UIParsing.parseEnum(GradientDirection.class), this::direction);
    }

    public enum GradientDirection {
        TOP_TO_BOTTOM, /*TOP_LEFT_TO_BOTTOM_RIGHT,*/
        RIGHT_TO_LEFT, /*TOP_RIGHT_TO_BOTTOM_LEFT,*/
        BOTTOM_TO_TOP, /*BOTTOM_RIGHT_TO_TOP_LEFT,*/
        LEFT_TO_RIGHT, /*BOTTOM_LEFT_TO_TOP_RIGHT*/
    }
}
