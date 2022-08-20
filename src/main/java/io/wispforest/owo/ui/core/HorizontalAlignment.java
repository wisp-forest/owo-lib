package io.wispforest.owo.ui.core;

import org.w3c.dom.Element;

import java.util.Locale;

public enum HorizontalAlignment {
    LEFT, CENTER, RIGHT;

    public int align(int componentWidth, int span) {
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> span / 2 - componentWidth / 2;
            case RIGHT -> span - componentWidth;
        };
    }

    public static HorizontalAlignment parse(Element element) {
        return valueOf(element.getTextContent().strip().toUpperCase(Locale.ROOT));
    }
}
