package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public record LabelStyle(@Nullable Alignment textAlignment, @Nullable Color baseColor, @Nullable Style textStyle, @Nullable Boolean shadow) {
    public static final LabelStyle EMPTY = new LabelStyle(null, null, null, null);
    public static final LabelStyle SHADOW = new LabelStyle(null, null, null, true);

    public LabelStyle overriding(LabelStyle other) {
        return new LabelStyle(
            this.textAlignment != null ? this.textAlignment : other.textAlignment,
            this.baseColor != null ? this.baseColor : other.baseColor,
            this.textStyle != null ? this.textStyle : other.textStyle,
            this.shadow != null ? this.shadow : other.shadow
        );
    }

    public LabelStyle fillDefaults() {
        return new LabelStyle(
            this.textAlignment != null ? this.textAlignment : Alignment.CENTER,
            this.baseColor != null ? this.baseColor : Color.WHITE,
            this.textStyle != null ? this.textStyle : Style.EMPTY,
            this.shadow != null ? this.shadow : false
        );
    }
}
