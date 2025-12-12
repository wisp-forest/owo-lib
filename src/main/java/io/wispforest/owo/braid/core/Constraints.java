package io.wispforest.owo.braid.core;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public record Constraints(double minWidth, double minHeight, double maxWidth, double maxHeight) {

    private static final Constraints UNCONSTRAINED = new Constraints(0, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Constraints {}

    public static Constraints unconstrained() {
        return UNCONSTRAINED;
    }

    public static Constraints of(double minWidth, double minHeight, double maxWidth, double maxHeight) {
        return new Constraints(minWidth, minHeight, maxWidth, maxHeight);
    }

    public static Constraints ofMinWidth(double minWidth) {
        return new Constraints(minWidth, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public static Constraints ofMinHeight(double minHeight) {
        return new Constraints(0, minHeight, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public static Constraints ofMaxWidth(double maxWidth) {
        return new Constraints(0, 0, maxWidth, Double.POSITIVE_INFINITY);
    }

    public static Constraints ofMaxHeight(double maxHeight) {
        return new Constraints(0, 0, Double.POSITIVE_INFINITY, maxHeight);
    }

    public static Constraints only(@Nullable Double minWidth, @Nullable Double minHeight, @Nullable Double maxWidth, @Nullable Double maxHeight) {
        return new Constraints(
            minWidth != null ? minWidth : 0,
            minHeight != null ? minHeight : 0,
            maxWidth != null ? maxWidth : Double.POSITIVE_INFINITY,
            maxHeight != null ? maxHeight : Double.POSITIVE_INFINITY
        );
    }

    public static Constraints tight(Size exactSize) {
        return new Constraints(exactSize.width(), exactSize.height(), exactSize.width(), exactSize.height());
    }

    public static Constraints loose(Size maxSize) {
        return new Constraints(0, 0, maxSize.width(), maxSize.height());
    }

    public static Constraints tightOnAxis(@Nullable Double horizontal, @Nullable Double vertical) {
        return only(horizontal, vertical, horizontal, vertical);
    }

    // ---

    public Constraints withMinWidth(double minWidth) {
        return new Constraints(minWidth, this.minHeight, this.maxWidth, this.maxHeight);
    }

    public Constraints withMinHeight(double minHeight) {
        return new Constraints(this.minWidth, minHeight, this.maxWidth, this.maxHeight);
    }

    public Constraints withMaxWidth(double maxWidth) {
        return new Constraints(this.minWidth, this.minHeight, maxWidth, this.maxHeight);
    }

    public Constraints withMaxHeight(double maxHeight) {
        return new Constraints(this.minWidth, this.minHeight, this.maxWidth, maxHeight);
    }

    // ---

    public double minOnAxis(LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> this.minWidth();
            case VERTICAL -> this.minHeight();
        };
    }

    public double maxOnAxis(LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> this.maxWidth();
            case VERTICAL -> this.maxHeight();
        };
    }

    public double maxFiniteOrMinOnAxis(LayoutAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> this.maxFiniteOrMinWidth();
            case VERTICAL -> this.maxFiniteOrMinHeight();
        };
    }

    public double maxFiniteOrMinWidth() {
        return this.hasBoundedWidth() ? this.maxWidth() : this.minWidth();
    }

    public double maxFiniteOrMinHeight() {
        return this.hasBoundedHeight() ? this.maxHeight() : this.minHeight();
    }

    // ---

    public Constraints asLoose() {
        return this.isLoose() ? this : new Constraints(0, 0, this.maxWidth, this.maxHeight);
    }

    public Constraints respecting(Constraints other) {
        if (this.minWidth >= other.minWidth && this.minWidth <= other.maxWidth
            && this.maxWidth >= other.minWidth && this.maxWidth <= other.maxWidth
            && this.minHeight >= other.minHeight && this.minHeight <= other.maxHeight
            && this.maxHeight >= other.minHeight && this.maxHeight <= other.maxHeight) {
            return this;
        }

        return new Constraints(
            Mth.clamp(this.minWidth, other.minWidth, other.maxWidth),
            Mth.clamp(this.minHeight, other.minHeight, other.maxHeight),
            Mth.clamp(this.maxWidth, other.minWidth, other.maxWidth),
            Mth.clamp(this.maxHeight, other.minHeight, other.maxHeight)
        );
    }

    public boolean hasLooseWidth() {
        return this.minWidth == 0;
    }

    public boolean hasLooseHeight() {
        return this.minHeight == 0;
    }

    public boolean hasTightWidth() {
        return this.minWidth == this.maxWidth;
    }

    public boolean hasTightHeight() {
        return this.minHeight == this.maxHeight;
    }

    public boolean isLoose() {
        return this.hasLooseWidth() && this.hasLooseHeight();
    }

    public boolean isTight() {
        return this.hasTightWidth() && this.hasTightHeight();
    }

    public boolean hasBoundedWidth() {
        return this.maxWidth < Double.POSITIVE_INFINITY;
    }

    public boolean hasBoundedHeight() {
        return this.maxHeight < Double.POSITIVE_INFINITY;
    }

    public Size minSize() {
        return Size.of(this.minWidth, this.minHeight);
    }

    public Size maxSize() {
        return Size.of(this.maxWidth, this.maxHeight);
    }

    public Size maxFiniteOrMinSize() {
        return Size.of(
            this.maxFiniteOrMinWidth(),
            this.maxFiniteOrMinHeight()
        );
    }
}
