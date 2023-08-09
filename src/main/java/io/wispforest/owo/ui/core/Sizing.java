package io.wispforest.owo.ui.core;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class Sizing implements Animatable<Sizing> {

    private static final Sizing CONTENT_SIZING = new Sizing(0, Method.CONTENT);

    public final Method method;
    public final int value;

    private Sizing(int value, Method method) {
        this.method = method;
        this.value = value;
    }

    /**
     * Inflate into the given space
     *
     * @param space               The available space
     * @param contentSizeFunction A function for making the component set the
     *                            size based on its content
     */
    public int inflate(int space, Function<Sizing, Integer> contentSizeFunction) {
        return switch (this.method) {
            case FIXED -> this.value;
            case FILL -> Math.round((this.value / 100f) * space);
            case CONTENT -> contentSizeFunction.apply(this) + this.value * 2;
        };
    }

    public static Sizing fixed(int value) {
        return new Sizing(value, Method.FIXED);
    }

    /**
     * Dynamically size the component based on its content,
     * without any padding
     */
    public static Sizing content() {
        return CONTENT_SIZING;
    }

    /**
     * Dynamically size the component based on its content
     *
     * @param padding Padding to add onto the size of the content
     */
    public static Sizing content(int padding) {
        return new Sizing(padding, Method.CONTENT);
    }
    
    /**
     * Dynamically size the component to fill the available space
     */
    public static Sizing fill() {
        return fill(100);
    }

    /**
     * Dynamically size the component based on the available space
     *
     * @param percent How many percent of the available space to take up
     */
    public static Sizing fill(int percent) {
        return new Sizing(percent, Method.FILL);
    }

    public boolean isContent() {
        return this.method == Method.CONTENT;
    }

    @Override
    public Sizing interpolate(Sizing next, float delta) {
        if (next.method != this.method) {
            Owo.LOGGER.warn("Cannot interpolate between sizing with method " + this.method + " and " + next.method);
            return this;
        }

        return new Sizing((int) MathHelper.lerp(delta, this.value, next.value), this.method);
    }

    public enum Method {
        FIXED, CONTENT, FILL
    }

    public static Sizing parse(Element sizingElement) {
        var methodString = sizingElement.getAttribute("method");
        if (methodString.isBlank()) {
            throw new UIModelParsingException("Missing 'method' attribute on sizing declaration. Must be one of: fixed, content, fill");
        }

        var method = Method.valueOf(methodString.toUpperCase(Locale.ROOT));
        var value = sizingElement.getTextContent().strip();

        if (method == Method.CONTENT) {
            if (!value.matches("(-?\\d+)?")) {
                throw new UIModelParsingException("Invalid value in sizing declaration");
            }

            return new Sizing(value.isEmpty() ? 0 : Integer.parseInt(value), method);
        } else {
            if (!value.matches("-?\\d+")) {
                throw new UIModelParsingException("Invalid value in sizing declaration");
            }

            return new Sizing(Integer.parseInt(value), method);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sizing sizing = (Sizing) o;
        return value == sizing.value && method == sizing.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, value);
    }

}
