package io.wispforest.owo.braid.core;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;

public class Color {

    public static final Color RED = Color.values(1, 0, 0);
    public static final Color YELLOW = Color.values(1, 1, 0);
    public static final Color GREEN = Color.values(0, 1, 0);
    public static final Color AQUA = Color.values(0, 1, 1);
    public static final Color BLUE = Color.values(0, 0, 1);
    public static final Color MAGENTA = Color.values(1, 0, 1);
    public static final Color WHITE = Color.values(1, 1, 1);
    public static final Color BLACK = Color.values(0, 0, 0);

    //

    public final double r, g, b, a;

    private Color(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    // ---

    public Color(int argb) {
        this(
            ((argb >> 16) & 0xFF) / 255.0,
            ((argb >> 8) & 0xFF) / 255.0,
            (argb & 0xFF) / 255.0,
            (argb >>> 24) / 255.0
        );
    }

    public static Color values(double r, double g, double b, double a) {
        return new Color(r, g, b, a);
    }

    public static Color values(double r, double g, double b) {
        return values(r, g, b, 1);
    }

    public static Color rgb(int rgb) {
        return values(
            ((rgb >> 16) & 0xFF) / 255.0,
            ((rgb >> 8) & 0xFF) / 255.0,
            (rgb & 0xFF) / 255.0
        );
    }

    public static Color hsv(double hue, double saturation, double value, double alpha) {
        // we call .5e-7f the magic "do not turn a hue value of 1f into yellow" constant
        return new Color((int) (alpha * 255) << 24 | Mth.hsvToRgb((float) (hue - .5e-7f), (float) saturation, (float) value));
    }

    public static Color hsv(double hue, double saturation, double value) {
        return hsv(hue, saturation, value, 1);
    }

    public static Color formatting(ChatFormatting formatting) {
        var rgb = formatting.getColor();
        return rgb(rgb != null ? rgb : 0);
    }

    public static Color mix(double t, Color a, Color b) {
        return Color.values(
            Mth.lerp(t, a.r, b.r),
            Mth.lerp(t, a.g, b.g),
            Mth.lerp(t, a.b, b.b),
            Mth.lerp(t, a.a, b.a)
        );
    }

    public static Color randomHue() {
        return hsv(Math.random(), .75, 1);
    }

    // ---

    public io.wispforest.owo.ui.core.Color toOwoUi() {
        return new io.wispforest.owo.ui.core.Color(
            (float) this.r, (float) this.g, (float) this.b, (float) this.a
        );
    }

    public String toHexString(boolean includeAlpha) {
        return includeAlpha
            ? String.format("#%08X", this.argb())
            : String.format("#%06X", this.rgb());
    }

    //

    public Color withR(double r) {
        return new Color(r, this.g, this.b, this.a);
    }

    public Color withG(double g) {
        return new Color(this.r, g, this.b, this.a);
    }

    public Color withB(double b) {
        return new Color(this.r, this.g, b, this.a);
    }

    public Color withA(double a) {
        return new Color(this.r, this.g, this.b, a);
    }

    // ---

    public int rgb() {
        return (int) (this.r * 255) << 16
            | (int) (this.g * 255) << 8
            | (int) (this.b * 255);
    }

    public int argb() {
        return (int) (this.a * 255) << 24
            | (int) (this.r * 255) << 16
            | (int) (this.g * 255) << 8
            | (int) (this.b * 255);
    }

    public float[] hsv() {
        return this.toOwoUi().hsv();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;

        var other = (Color) o;
        return this.r == other.r
            && this.g == other.g
            && this.b == other.b
            && this.a == other.a;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(r);
        result = 31 * result + Double.hashCode(g);
        result = 31 * result + Double.hashCode(b);
        result = 31 * result + Double.hashCode(a);
        return result;
    }
}
