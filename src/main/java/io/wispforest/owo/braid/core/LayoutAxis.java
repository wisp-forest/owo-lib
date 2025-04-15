package io.wispforest.owo.braid.core;

public enum LayoutAxis {
    HORIZONTAL,
    VERTICAL;

    public <T> T choose(T horizontal, T vertical) {
        return switch (this) {
            case HORIZONTAL -> horizontal;
            case VERTICAL -> vertical;
        };
    }

    public Size createSize(double extent, double crossExtent) {
        return switch (this) {
            case HORIZONTAL -> Size.of(extent, crossExtent);
            case VERTICAL -> Size.of(crossExtent, extent);
        };
    }

    public LayoutAxis opposite() {
        return switch (this) {
            case HORIZONTAL -> VERTICAL;
            case VERTICAL -> HORIZONTAL;
        };
    }
}
