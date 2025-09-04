package io.wispforest.owo.braid.widgets.slider.drag;

import org.jetbrains.annotations.Nullable;

public interface DragFunction {

    /**
     * Map a normalized cursor delta (unitless, where 1.0 equals full track length) to a value delta
     * for the given current value and optional bounds.
     */
    double deltaValue(double currentValue, @Nullable Double min, @Nullable Double max, double cursorNormalizedDelta);

    DragFunction LINEAR = (currentValue, min, max, cursorDelta) -> {
        if (min != null && max != null) return cursorDelta * (max - min);
        return cursorDelta; // unbounded: treat 1.0 as one unit and let dragMultiplier/step scale it
    };

    /**
     * Produces larger value deltas the further |currentValue| is from 0. For bounded ranges, the scale
     * is computed relative to the larger magnitude bound to keep behavior consistent for asymmetric ranges.
     */
    DragFunction LOGARITHMIC = (currentValue, min, max, cursorDelta) -> {
        double base;
        if (min != null && max != null) {
            base = cursorDelta * (max - min);
            double denom = Math.max(Math.abs(min), Math.abs(max));
            double rel = denom > 0 ? Math.abs(currentValue) / denom : 0;
            double scale = 1.0 + rel; // linear growth with distance from 0 in [1,2]
            return base * scale;
        } else {
            // Unbounded: scale by log-like factor using absolute value as heuristic
            double scale = 1.0 + Math.min(2.0, Math.log1p(Math.abs(currentValue)));
            return cursorDelta * scale;
        }
    };
}


