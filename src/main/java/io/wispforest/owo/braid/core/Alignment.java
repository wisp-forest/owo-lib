package io.wispforest.owo.braid.core;

import org.jetbrains.annotations.ApiStatus;

public record Alignment(double horizontal, double vertical) {
    public static final Alignment TOP_LEFT = Alignment.of(0, 0);
    public static final Alignment TOP = Alignment.of(.5, 0);
    public static final Alignment TOP_RIGHT = Alignment.of(1, 0);
    public static final Alignment LEFT = Alignment.of(0, .5);
    public static final Alignment CENTER = Alignment.of(.5, .5);
    public static final Alignment RIGHT = Alignment.of(1, .5);
    public static final Alignment BOTTOM_LEFT = Alignment.of(0, 1);
    public static final Alignment BOTTOM = Alignment.of(.5, 1);
    public static final Alignment BOTTOM_RIGHT = Alignment.of(1, 1);

    // ---

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Alignment {}

    public static Alignment of(double horizontal, double vertical) {
        return new Alignment(horizontal, vertical);
    }

    public double alignHorizontal(double space, double object) {
        return Math.floor((space - object) * horizontal);
    }

    public double alignVertical(double space, double object) {
        return Math.floor((space - object) * vertical);
    }
}
