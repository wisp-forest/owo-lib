package io.wispforest.owo.braid.widgets.splitpane;

import java.util.Arrays;

import static com.mojang.math.Constants.EPSILON;

public interface ResizeDistribution {
    double distribute(double[] sizes, double delta);

    ResizeDistribution ALL = (sizes, delta) -> {
        var total = Arrays.stream(sizes).sum();
        if (total > 0)
            for (var i = 0; i < sizes.length; i++)
                sizes[i] = Math.max(0, sizes[i] * (total + delta) / total);
        return 0;
    };

    ResizeDistribution FIRST = (sizes, delta) -> {
        sizes[0] += delta;
        return 0;
    };

    ResizeDistribution LAST = (sizes, delta) -> {
        sizes[sizes.length - 1] += delta;
        return 0;
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
