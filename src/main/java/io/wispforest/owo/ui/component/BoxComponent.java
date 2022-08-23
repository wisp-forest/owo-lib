package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.Color;
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

    protected AnimatableProperty<Color> startColor = AnimatableProperty.of(Color.BLACK);
    protected AnimatableProperty<Color> endColor = AnimatableProperty.of(Color.BLACK);

    public BoxComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this.sizing(horizontalSizing, verticalSizing);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.startColor.update(delta);
        this.endColor.update(delta);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        final int startColor = this.startColor.get().argb();
        final int endColor = this.endColor.get().argb();

        if (this.fill) {
            switch (this.direction) {
                case TOP_TO_BOTTOM -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        startColor, startColor, endColor, endColor);
                case RIGHT_TO_LEFT -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        endColor, startColor, startColor, endColor);
                case BOTTOM_TO_TOP -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        endColor, endColor, startColor, startColor);
                case LEFT_TO_RIGHT -> Drawer.drawGradientRect(matrices, this.x, this.y, this.width, this.height,
                        startColor, endColor, endColor, startColor);
            }
        } else {
            Drawer.drawRectOutline(matrices, this.x, this.y, this.width, this.height, startColor);
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

    public BoxComponent color(Color color) {
        this.startColor.set(color);
        this.endColor.set(color);
        return this;
    }

    public BoxComponent startColor(Color startColor) {
        this.startColor.set(startColor);
        return this;
    }

    public AnimatableProperty<Color> startColor() {
        return this.startColor;
    }

    public BoxComponent endColor(Color endColor) {
        this.endColor.set(endColor);
        return this;
    }

    public AnimatableProperty<Color> endColor() {
        return this.endColor;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.expectChildren(element, children, "sizing");

        UIParsing.apply(children, "color", Color::parse, this::color);
        UIParsing.apply(children, "start-color", Color::parse, this::startColor);
        UIParsing.apply(children, "end-color", Color::parse, this::endColor);
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
