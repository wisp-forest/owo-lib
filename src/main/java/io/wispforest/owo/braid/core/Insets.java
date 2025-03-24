package io.wispforest.owo.braid.core;

import org.jetbrains.annotations.ApiStatus;

public record Insets(double top, double bottom, double left, double right) {

    private static final Insets NONE = new Insets(0, 0, 0, 0);

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Insets {}

    // ---

    public static Insets of(double top, double bottom, double left, double right) {
        return new Insets(top, bottom, left, right);
    }

    public static Insets all(double inset) {
        return new Insets(inset, inset, inset, inset);
    }

    public static Insets both(double horizontal, double vertical) {
        return new Insets(vertical, vertical, horizontal, horizontal);
    }

    public static Insets top(double top) {
        return new Insets(top, 0, 0, 0);
    }

    public static Insets bottom(double bottom) {
        return new Insets(0, bottom, 0, 0);
    }

    public static Insets left(double left) {
        return new Insets(0, 0, left, 0);
    }

    public static Insets right(double right) {
        return new Insets(0, 0, 0, right);
    }

    public static Insets vertical(double inset) {
        return new Insets(inset, inset, 0, 0);
    }

    public static Insets horizontal(double inset) {
        return new Insets(0, 0, inset, inset);
    }

    public static Insets none() {
        return NONE;
    }

    // ---

    public Insets withTop(double top) {
        return new Insets(top, this.bottom, this.left, this.right);
    }

    public Insets withBottom(double bottom) {
        return new Insets(this.top, bottom, this.left, this.right);
    }

    public Insets withLeft(double left) {
        return new Insets(this.top, this.bottom, left, this.right);
    }

    public Insets withRight(double right) {
        return new Insets(this.top, this.bottom, this.left, right);
    }

    public double horizontal() {
        return this.left + this.right;
    }

    public double vertical() {
        return this.top + this.bottom;
    }
}
