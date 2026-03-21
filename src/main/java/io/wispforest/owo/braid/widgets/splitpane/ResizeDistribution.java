package io.wispforest.owo.braid.widgets.splitpane;

import static com.mojang.math.Constants.EPSILON;

public interface ResizeDistribution {
    double distribute(double[] sizes, double delta);

    ResizeDistribution ALL = (sizes, delta) -> {
        var total = 0d;
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
        var max = 0d;
        for (var s : sizes) if (s > max) max = s;
        var count = 0;
        for (var s : sizes) if (s >= max - EPSILON) count++;
        var next = 0d;
        for (var s : sizes) if (s > next && s < max - EPSILON) next = s;

        var cap = (max - next) * count;
        var absorbed = delta < 0 ? Math.max(delta, -cap) : delta;
        for (var i = 0; i < sizes.length; i++)
            if (sizes[i] >= max - EPSILON) sizes[i] += absorbed / count;
        return delta - absorbed;
    };

    ResizeDistribution SMALLEST = (sizes, delta) -> {
        var minVal = Double.MAX_VALUE;
        for (var s : sizes) if (s < minVal) minVal = s;
        if (minVal == Double.MAX_VALUE) return delta;
        var count = 0;
        for (var s : sizes) if (s <= minVal + EPSILON) count++;
        var nextLevel = Double.MAX_VALUE;
        for (var s : sizes) if (s > minVal + EPSILON && s < nextLevel) nextLevel = s;

        var cap = nextLevel == Double.MAX_VALUE ? delta : Math.min(delta, (nextLevel - minVal) * count);
        var absorbed = delta > 0 ? cap : delta;
        for (var i = 0; i < sizes.length; i++)
            if (sizes[i] <= minVal + EPSILON) sizes[i] += absorbed / count;
        return delta - absorbed;
    };
}
