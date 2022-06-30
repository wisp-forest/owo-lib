package io.wispforest.owo.ui.definitions;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.Locale;

// TODO more doccs
public class Positioning implements Animatable<Positioning> {

    private static final Positioning LAYOUT_POSITIONING = new Positioning(0, 0, Type.LAYOUT);

    public final Type type;
    public final int x, y;

    private Positioning(int x, int y, Type type) {
        this.type = type;
        this.x = x;
        this.y = y;
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

    public static Positioning absolute(int xPixels, int yPixels) {
        return new Positioning(xPixels, yPixels, Type.ABSOLUTE);
    }

    public static Positioning relative(int xPercent, int yPercent) {
        return new Positioning(xPercent, yPercent, Type.RELATIVE);
    }

    public static Positioning layout() {
        return LAYOUT_POSITIONING;
    }

    public enum Type {
        RELATIVE, ABSOLUTE, LAYOUT;
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

}
