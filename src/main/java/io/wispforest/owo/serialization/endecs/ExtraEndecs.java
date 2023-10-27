package io.wispforest.owo.serialization.endecs;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.ReflectionEndecBuilder;
import net.minecraft.recipe.book.CraftingRecipeCategory;

import java.util.function.Function;

public class ExtraEndecs {

    public static final Endec<Integer> NONNEGATIVE_INT = rangedInt(0, Integer.MAX_VALUE, v -> "Value must be non-negative: " + v);
    public static final Endec<Integer> POSITIVE_INT = rangedInt(1, Integer.MAX_VALUE, v -> "Value must be positive: " + v);


    private static Endec<Integer> rangedInt(int min, int max, Function<Integer, String> messageFactory) {
        return Endec.INT.validate(value -> {
            if(value.compareTo(min) >= 0 && value.compareTo(max) <= 0) return value;

            throw new IllegalStateException(messageFactory.apply(value));
        });
    }
}
