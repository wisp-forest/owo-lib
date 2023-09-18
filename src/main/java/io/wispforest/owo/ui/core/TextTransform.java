package io.wispforest.owo.ui.core;

import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Element;

import java.util.Locale;
import java.util.function.Function;

public enum TextTransform {
    NONE(str -> str),
    LOWERCASE(String::toLowerCase),
    UPPERCASE(String::toUpperCase),
    CAPITALIZE(WordUtils::capitalizeFully);

    private final Function<String, String> function;

    TextTransform(Function<String, String> function) {
        this.function = function;
    }

    public String apply(String src) {
        return function.apply(src);
    }

    public static TextTransform parse(Element element) {
        return valueOf(element.getTextContent().strip().toUpperCase(Locale.ROOT));
    }
}
