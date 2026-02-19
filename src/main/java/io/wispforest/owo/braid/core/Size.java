package io.wispforest.owo.braid.core;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public record Size(double width, double height) {

    private static final Size ZERO = new Size(0, 0);

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Size {}

    // ---

    public static Size zero() {
        return ZERO;
    }

    public static Size of(double width, double height) {
        return new Size(width, height);
    }

    public static Size square(double sideLength) {
        return new Size(sideLength, sideLength);
    }

    public static Size max(Size a, Size b) {
        return new Size(Math.max(a.width, b.width), Math.max(a.height, b.height));
    }

    // ---

    public Size withInsets(Insets insets) {
        return new Size(this.width + insets.horizontal(), this.height + insets.vertical());
    }

    public Size with(@Nullable Double width, @Nullable Double height) {
        return new Size(width != null ? width : this.width, height != null ? height : this.height);
    }

    public Size floor() {
        return new Size(Math.floor(this.width), Math.floor(this.height));
    }

    public Size ceil() {
        return new Size(Math.ceil(this.width), Math.ceil(this.height));
    }

    public double getExtent(LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> width();
            case VERTICAL -> height();
        };
    }

    public Size constrained(Constraints constraints) {
        return new Size(
            Mth.clamp(this.width, constraints.minWidth(), constraints.maxWidth()),
            Mth.clamp(this.height, constraints.minHeight(), constraints.maxHeight())
        );
    }
}