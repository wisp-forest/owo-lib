package io.wispforest.owo.ui.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a two-dimensional value, used for
 * describing position-less rectangles in 2D-space
 *
 * @param width  The width of the rectangle
 * @param height The height of the rectangle
 */
public record Size(int width, int height) {

    public static final Endec<Size> ENDEC =StructEndecBuilder.of(
            Endec.INT.fieldOf("width", Size::width),
            Endec.INT.fieldOf("height", Size::height),
            Size::of
    );

    private static final Size ZERO = new Size(0, 0);

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Size {}

    public static Size of(int width, int height) {
        return new Size(width, height);
    }

    public static Size square(int sideLength) {
        return new Size(sideLength, sideLength);
    }

    /**
     * @return A size with both values equal to 0
     */
    public static Size zero() {
        return ZERO;
    }
}
