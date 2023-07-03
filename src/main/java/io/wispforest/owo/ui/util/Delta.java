package io.wispforest.owo.ui.util;

/**
 * Trying to give this utility class a
 * sensible name makes me mald
 */
public final class Delta {

    private Delta() {}

    /**
     * Compute an additive interpolator for smoothly approaching the
     * target value given the current value and some interpolation
     * delta
     *
     * @param current The current value
     * @param target  The target value to approach
     * @param delta   The interpolation delta - this is usually the frame delta,
     *                optionally multiplied by some factor
     * @return The computed interpolator, to be added to the current value
     */
    public static float compute(float current, float target, float delta) {
        float diff = target - current;
        delta = diff * delta;

        return Math.abs(delta) > Math.abs(diff) ? diff : delta;
    }

    /**
     * Compute an additive interpolator for smoothly approaching the
     * target value given the current value and some interpolation
     * delta
     *
     * @param current The current value
     * @param target  The target value to approach
     * @param delta   The interpolation delta - this is usually the frame delta,
     *                optionally multiplied by some factor
     * @return The computed interpolator, to be added to the current value
     */
    public static double compute(double current, double target, double delta) {
        double diff = target - current;
        delta = diff * delta;

        return Math.abs(delta) > Math.abs(diff) ? diff : delta;
    }
}
