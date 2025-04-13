package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.ui.core.Color;

public record LabelStyle(Alignment textAlignment, Color baseColor, boolean shadow) {
    public static final LabelStyle DEFAULT = new LabelStyle(Alignment.CENTER, Color.WHITE, false);
}
