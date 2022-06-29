package io.wispforest.owo.ui.definitions;

/**
 * An easing function which can smoothly move
 * an interpolation value from 0 to 1
 */
public interface Easing {

    Easing LINEAR = x -> x;

    Easing SINE = x -> {
        return (float) (Math.sin(x * Math.PI - Math.PI / 2) * 0.5 + 0.5);
    };

    Easing QUADRATIC = x -> {
        return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
    };

    Easing CUBIC = x -> {
        return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
    };

    float apply(float x);

}
