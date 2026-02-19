package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * A colored rectangle either filled or outlined
 * by a given color or gradient
 */
public class BoxComponent extends BaseUIComponent {

    protected boolean fill = false;
    protected GradientDirection direction = GradientDirection.TOP_TO_BOTTOM;

    protected final AnimatableProperty<Color> startColor = AnimatableProperty.of(Color.BLACK);
    protected final AnimatableProperty<Color> endColor = AnimatableProperty.of(Color.BLACK);

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
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        final int startColor = this.startColor.get().argb();
        final int endColor = this.endColor.get().argb();

        if (this.fill) {
            switch (this.direction) {
                case TOP_TO_BOTTOM -> graphics.drawGradientRect(this.x, this.y, this.width, this.height,
                        startColor, startColor, endColor, endColor);
                case RIGHT_TO_LEFT -> graphics.drawGradientRect(this.x, this.y, this.width, this.height,
                        endColor, startColor, startColor, endColor);
                case BOTTOM_TO_TOP -> graphics.drawGradientRect(this.x, this.y, this.width, this.height,
                        endColor, endColor, startColor, startColor);
                case LEFT_TO_RIGHT -> graphics.drawGradientRect(this.x, this.y, this.width, this.height,
                        startColor, endColor, endColor, startColor);
            }
        } else {
            graphics.drawRectOutline(this.x, this.y, this.width, this.height, startColor);
        }
    }

    /**
     * Set whether this component should be
     * filled with color or outlined
     */
    public BoxComponent fill(boolean fill) {
        this.fill = fill;
        return this;
    }

    /**
     * @return {@code true} if this component is currently
     * filled with color, {@code false} if it is outlined
     */
    public boolean fill() {
        return this.fill;
    }

    /**
     * Set the direction in which the gradient inside
     * this component should travel
     */
    public BoxComponent direction(GradientDirection direction) {
        this.direction = direction;
        return this;
    }

    /**
     * @return The direction in which the gradient inside
     * this component currently travels
     */
    public GradientDirection direction() {
        return this.direction;
    }

    /**
     * Set the color of this component. Equivalent to calling
     * both {@link #startColor(Color)} and {@link #endColor(Color)}
     *
     * @param color The start and end color of this
     *              component's color gradient
     */
    public BoxComponent color(Color color) {
        this.startColor.set(color);
        this.endColor.set(color);
        return this;
    }

    /**
     * Set the start color of this component's gradient
     */
    public BoxComponent startColor(Color startColor) {
        this.startColor.set(startColor);
        return this;
    }

    /**
     * @return The current start color of this component's gradient
     */
    public AnimatableProperty<Color> startColor() {
        return this.startColor;
    }

    /**
     * Set the end color of this component's gradient
     */
    public BoxComponent endColor(Color endColor) {
        this.endColor.set(endColor);
        return this;
    }

    /**
     * @return The current end color of this component's gradient
     */
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
