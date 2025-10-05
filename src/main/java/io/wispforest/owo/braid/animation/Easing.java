package io.wispforest.owo.braid.animation;

public class Easing {

    public static final Easing LINEAR = new Easing(x -> x);
    public static final Easing IN_QUAD = new Easing(x -> x * x);
    public static final Easing OUT_QUAD = new Easing(x -> 1.0 - (1.0 - x) * (1.0 - x));
    public static final Easing IN_OUT_QUAD = new Easing(x -> x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0);
    public static final Easing IN_CUBIC = new Easing(x -> x * x * x);
    public static final Easing OUT_CUBIC = new Easing(x -> 1.0 - Math.pow(1.0 - x, 3));
    public static final Easing IN_OUT_CUBIC = new Easing(x -> x < 0.5 ? 4.0 * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 3.0) / 2.0);
    public static final Easing IN_QUART = new Easing(x -> x * x * x * x);
    public static final Easing OUT_QUART = new Easing(x -> 1.0 - Math.pow(1.0 - x, 4.0));
    public static final Easing IN_OUT_QUART = new Easing(x -> x < 0.5 ? 8.0 * x * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 4.0) / 2.0);
    public static final Easing IN_QUINT = new Easing(x -> x * x * x * x * x);
    public static final Easing OUT_QUINT = new Easing(x -> 1.0 - Math.pow(1.0 - x, 5.0));
    public static final Easing IN_OUT_QUINT = new Easing(x -> x < 0.5 ? 16.0 * x * x * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 5.0) / 2.0);
    public static final Easing IN_SINE = new Easing(x -> 1.0 - Math.cos((x * Math.PI) / 2.0));
    public static final Easing OUT_SINE = new Easing(x -> Math.sin((x * Math.PI) / 2.0));
    public static final Easing IN_OUT_SINE = new Easing(x -> -(Math.cos(Math.PI * x) - 1) / 2.0);
    public static final Easing IN_EXPO = new Easing(x -> x == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * x - 10.0));
    public static final Easing OUT_EXPO = new Easing(x -> x == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * x));
    public static final Easing IN_OUT_EXPO = new Easing(x -> x == 0.0 ? 0.0 : x == 1.0 ? 1.0 : x < 0.5 ? Math.pow(2.0, 20.0 * x - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0);
    public static final Easing IN_CIRC = new Easing(x -> 1.0 - Math.sqrt(1.0 - Math.pow(x, 2.0)));
    public static final Easing OUT_CIRC = new Easing(x -> Math.sqrt(1.0 - Math.pow(x - 1.0, 2.0)));
    public static final Easing IN_OUT_CIRC = new Easing(x -> x < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * x, 2.0))) / 2 : (Math.sqrt(1.0 - Math.pow(-2.0 * x + 2.0, 2.0)) + 1.0) / 2.0);

    public static final Easing OUT_BOUNCE = new Easing(x -> {
        var n1 = 7.5625;
        var d1 = 2.75;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            return n1 * (x -= 1.5 / d1) * x + 0.75;
        } else if (x < 2.5 / d1) {
            return n1 * (x -= 2.25 / d1) * x + 0.9375;
        } else {
            return n1 * (x -= 2.625 / d1) * x + 0.984375;
        }
    });

    // ---

    private final Function function;
    public Easing(Function function) {
        this.function = function;
    }

    public final double apply(double x) {
        if (x == 0 || x == 1) return x;
        return this.compute(x);
    }

    protected double compute(double x) {
        return this.function.compute(x);
    }

    @FunctionalInterface
    public interface Function {
        double compute(double x);
    }
}