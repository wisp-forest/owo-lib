package io.wispforest.owo.config;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.Internal
public class ConfigPredicates {

    public static final Predicate<String> IDENTIFIER_INPUT = s -> s.matches("[a-z0-9_.:\\-]*");
    public static final Predicate<String> IDENTIFIER_APPLY = s -> Identifier.tryParse(s) != null;

    public static Predicate<String> numberInput(boolean floatingPoint) {
        return s -> s.matches(floatingPoint ? "-?\\d*\\.?\\d*" : "-?\\d*");
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

    public static Predicate<String> hexColorInput(boolean withAlpha) {
        return s -> s.matches(withAlpha ? "#[a-zA-Z\\d]{0,8}" : "#[a-zA-Z\\d]{0,6}");
    }

    public static Predicate<String> hexColorApply(boolean withAlpha) {
        return s -> s.matches(withAlpha ? "#[a-zA-Z\\d]{8}" : "#[a-zA-Z\\d]{6}");
    }
}
