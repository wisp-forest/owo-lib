package io.wispforest.owo.braid.core;

import java.util.function.BiFunction;

public class BraidUtils {
    public static <S, T> T fold(Iterable<S> values, T initial, BiFunction<T, S, T> step) {
        var result = initial;
        for (var value : values) {
            result = step.apply(result, value);
        }

        return result;
    }
}
