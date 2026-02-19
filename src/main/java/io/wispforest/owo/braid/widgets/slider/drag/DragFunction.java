package io.wispforest.owo.braid.widgets.slider.drag;

import org.jetbrains.annotations.Nullable;

public interface DragFunction {

    double deltaValue(double currentValue, @Nullable Double min, @Nullable Double max, double cursorNormalizedDelta);

    DragFunction LINEAR = (currentValue, min, max, cursorDelta) -> {
        if (min != null && max != null) return cursorDelta * (max - min);
        return cursorDelta;
    };

    DragFunction LOGARITHMIC = (currentValue, min, max, cursorDelta) -> {
        double base;
        if (min != null && max != null) {
            base = cursorDelta * (max - min);
            double denom = Math.max(Math.abs(min), Math.abs(max));
            double rel = denom > 0 ? Math.abs(currentValue) / denom : 0;
            double scale = 1.0 + rel;
            return base * scale;
        } else {
            double scale = 1.0 + Math.min(2.0, Math.log1p(Math.abs(currentValue)));
            return cursorDelta * scale;
        }
    };
}
