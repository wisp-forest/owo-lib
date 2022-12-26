package io.wispforest.owo.ui.core;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.Locale;
import java.util.Objects;

public class Positioning implements Animatable<Positioning> {

    private static final Positioning LAYOUT_POSITIONING = new Positioning(0, 0, Type.LAYOUT);

    public final Type type;
    public final int x, y;

    private Positioning(int x, int y, Type type) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public Positioning withX(int x) {
        return new Positioning(x, this.y, this.type);
    }

    public Positioning withY(int y) {
        return new Positioning(this.x, y, this.type);
    }

    @Override
    public Positioning interpolate(Positioning next, float delta) {
        if (next.type != this.type) {
            Owo.LOGGER.warn("Cannot interpolate between positioning of type " + this.type + " and " + next.type);
            return this;
        }

        return new Positioning(
                (int) MathHelper.lerp(delta, this.x, next.x),
                (int) MathHelper.lerp(delta, this.y, next.y),
                this.type
        );
    }

    /**
     * Position the component at an absolute offset
     * from the root of parent
     *
     * @param xPixels The offset on the x-axis
     * @param yPixels The offset on the y-axis
     */
    public static Positioning absolute(int xPixels, int yPixels) {
        return new Positioning(xPixels, yPixels, Type.ABSOLUTE);
    }

    /**
     * Position the component at a relative offset
     * inside the parent. This respect the size of
     * the component itself. As such:
     * <ul>
     *     <li>50,50 centers the component inside the parent</li>
     *     <li>100,50 centers to component vertically and pushes it all the way to the right</li>
     *     <li>100,100 pushes the component all the way into the bottom right corner of the parent</li>
     * </ul>
     *
     * @param xPercent The offset on the x-axis
     * @param yPercent The offset on the y-axis
     */
    public static Positioning relative(int xPercent, int yPercent) {
        return new Positioning(xPercent, yPercent, Type.RELATIVE);
    }

    /**
     * Position the component using whatever layout
     * method the parent component wants to apply
     */
    public static Positioning layout() {
        return LAYOUT_POSITIONING;
    }

    public enum Type {
        RELATIVE, ABSOLUTE, LAYOUT
    }

    public static Positioning parse(Element positioningElement) {
        var typeString = positioningElement.getAttribute("type");
        if (typeString.isBlank()) {
            throw new UIModelParsingException("Missing 'type' attribute on positioning declaration. Must be one of: relative, absolute, layout");
        }

        var type = Type.valueOf(typeString.toUpperCase(Locale.ROOT));

        var values = positioningElement.getTextContent().strip();
        if (!values.matches("-?\\d+,-?\\d+")) {
            throw new UIModelParsingException("Invalid value in positioning declaration");
        }

        int x = Integer.parseInt(values.split(",")[0]);
        int y = Integer.parseInt(values.split(",")[1]);

        return new Positioning(x, y, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Positioning that = (Positioning) o;
        return x == that.x && y == that.y && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x, y);
    }
}
