package io.wispforest.owo.braid.util.layers;

public record AnchorJustification(double anchorX, double anchorY, double widgetX, double widgetY) {
    public static final AnchorJustification TOP_LEFT_TO_TOP_LEFT = new AnchorJustification(0, 0, 0, 0);
    public static final AnchorJustification TOP_LEFT_TO_BOTTOM_LEFT = new AnchorJustification(0, 1, 0, 0);
    public static final AnchorJustification TOP_LEFT_TO_TOP_RIGHT = new AnchorJustification(1, 0, 0, 0);
    public static final AnchorJustification TOP_LEFT_TO_BOTTOM_RIGHT = new AnchorJustification(1, 1, 0, 0);

    public static final AnchorJustification BOTTOM_LEFT_TOP_LEFT = new AnchorJustification(0, 0, 0, 1);
    public static final AnchorJustification BOTTOM_LEFT_BOTTOM_LEFT = new AnchorJustification(0, 1, 0, 1);
    public static final AnchorJustification BOTTOM_LEFT_TOP_RIGHT = new AnchorJustification(1, 0, 0, 1);
    public static final AnchorJustification BOTTOM_LEFT_BOTTOM_RIGHT = new AnchorJustification(1, 1, 0, 1);

    public static final AnchorJustification TOP_RIGHT_TO_TOP_LEFT = new AnchorJustification(0, 0, 1, 0);
    public static final AnchorJustification TOP_RIGHT_TO_BOTTOM_LEFT = new AnchorJustification(0, 1, 1, 0);
    public static final AnchorJustification TOP_RIGHT_TO_TOP_RIGHT = new AnchorJustification(1, 0, 1, 0);
    public static final AnchorJustification TOP_RIGHT_TO_BOTTOM_RIGHT = new AnchorJustification(1, 1, 1, 0);

    public static final AnchorJustification BOTTOM_RIGHT_TO_TOP_LEFT = new AnchorJustification(0, 0, 1, 1);
    public static final AnchorJustification BOTTOM_RIGHT_TO_BOTTOM_LEFT = new AnchorJustification(0, 1, 1, 1);
    public static final AnchorJustification BOTTOM_RIGHT_TO_TOP_RIGHT = new AnchorJustification(1, 0, 1, 1);
    public static final AnchorJustification BOTTOM_RIGHT_TO_BOTTOM_RIGHT = new AnchorJustification(1, 1, 1, 1);

    public static final AnchorJustification CENTER_TO_TOP_LEFT = new AnchorJustification(0, 0, .5, .5);
    public static final AnchorJustification CENTER_TO_BOTTOM_LEFT = new AnchorJustification(0, 1, .5, .5);
    public static final AnchorJustification CENTER_TO_TOP_RIGHT = new AnchorJustification(1, 0, .5, .5);
    public static final AnchorJustification CENTER_TO_BOTTOM_RIGHT = new AnchorJustification(1, 1, .5, .5);
    public static final AnchorJustification TOP_LEFT_TO_CENTER = new AnchorJustification(.5, .5, 0, 0);
    public static final AnchorJustification BOTTOM_LEFT_TO_CENTER = new AnchorJustification(.5, .5, 0, 1);
    public static final AnchorJustification TOP_RIGHT_TO_CENTER = new AnchorJustification(.5, .5, 1, 0);
    public static final AnchorJustification BOTTOM_RIGHT_TO_CENTER = new AnchorJustification(.5, .5, 1, 1);
    public static final AnchorJustification CENTER_TO_CENTER = new AnchorJustification(.5, .5, .5, .5);
}
