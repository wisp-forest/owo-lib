package io.wispforest.owo.braid.framework.instance;

import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Collectors;

public record InspectorProperty(Component name, Component value) {

    public InspectorProperty(String name, String value) {
        this(Component.literal(name), Component.literal(value));
    }

    public static String rounded(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString().replaceAll("(\\.0*|(?<=\\d)\\.0+)$", "");
    }

    public static String roundedWithCommas(double... values) {
        return Arrays.stream(values)
            .mapToObj(InspectorProperty::rounded)
            .collect(Collectors.joining(", "));
    }
}
