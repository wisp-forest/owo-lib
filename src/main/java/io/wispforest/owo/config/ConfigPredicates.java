package io.wispforest.owo.config;

import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class ConfigPredicates {

    public static final Predicate<String> IDENTIFIER_INPUT = s -> s.matches("[a-z0-9_.:\\-]*");
    public static final Predicate<String> IDENTIFIER_APPLY = s -> Identifier.tryParse(s) != null;

    public static Predicate<String> numberInput(boolean floatingPoint) {
        return floatingPoint
            ? s -> s.matches("-?\\d*\\.?\\d*")
            : s -> s.matches("-?\\d*");
    }

    public static Predicate<String> numberApply(double min, double max) {
        return s -> {
            try {
                var value = Double.parseDouble(s);
                return value >= min && value <= max;
            } catch (NumberFormatException nfe) {
                return false;
            }
        };
    }
}
