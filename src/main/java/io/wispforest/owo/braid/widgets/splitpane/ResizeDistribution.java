package io.wispforest.owo.braid.widgets.splitpane;

import java.util.Arrays;

import static com.mojang.math.Constants.EPSILON;

public interface ResizeDistribution {
    double distribute(double[] sizes, double delta);

    ResizeDistribution ALL = (sizes, delta) -> {
        var total = Arrays.stream(sizes).sum();
        for (var size : sizes) total += size;
        if (total > 0) {
            var scale = (total + delta) / total;
            for (var i = 0; i < sizes.length; i++)
                sizes[i] = Math.max(0, sizes[i] * scale);
        }
        return 0;
    };

    ResizeDistribution FIRST = (sizes, delta) -> {
        for (var i = 0; i < sizes.length; i++) {
            if (sizes[i] > 0 || delta > 0) {
                var prev = sizes[i];
                sizes[i] = Math.max(0, sizes[i] + delta);
                return delta - (sizes[i] - prev);
            }
        }
        return delta;
    };

    ResizeDistribution LAST = (sizes, delta) -> {
        for (var i = sizes.length - 1; i >= 0; i--) {
            if (sizes[i] > 0 || delta > 0) {
                var prev = sizes[i];
                sizes[i] = Math.max(0, sizes[i] + delta);
                return delta - (sizes[i] - prev);
            }
        }
        return delta;
    };

    ResizeDistribution LARGEST = (sizes, delta) -> {
        final var max = Arrays.stream(sizes).max().orElse(0);
        final var next = Arrays.stream(sizes).filter(s -> s < max - EPSILON).max().orElse(0);
        var count = Arrays.stream(sizes).filter(s -> s >= max - EPSILON).count();
        if (count == 0) return delta;
        var absorbed = delta < 0 ? Math.max(delta, -(max - next) * count) : delta;
        for (var i = 0; i < sizes.length; i++)
            if (sizes[i] >= max - EPSILON) sizes[i] += absorbed / count;
        return delta - absorbed;
    };

    ResizeDistribution SMALLEST = (sizes, delta) -> {
        var min = Arrays.stream(sizes).min().orElse(0);
        var next = Arrays.stream(sizes).filter(s -> s > min + EPSILON).min().orElse(Double.MAX_VALUE);
        var count = Arrays.stream(sizes).filter(s -> s <= min + EPSILON).count();
        if (count == 0) return delta;
        var absorbed = delta > 0 && next != Double.MAX_VALUE ? Math.min(delta, (next - min) * count) : delta;
        for (var i = 0; i < sizes.length; i++)
            if (sizes[i] <= min + EPSILON) sizes[i] += absorbed / count;
        return delta - absorbed;
    };
}
