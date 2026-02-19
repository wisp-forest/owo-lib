package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public record ButtonStyle(
    @Nullable ContentBuilder builder,
    @Nullable Insets padding,
    @Nullable SoundEvent clickSound
) {
    public ButtonStyle overriding(ButtonStyle other) {
        return new ButtonStyle(
            this.builder != null ? this.builder : other.builder,
            this.padding != null ? this.padding : other.padding,
            this.clickSound != null ? this.clickSound : other.clickSound
        );
    }

    public static final ButtonStyle DEFAULT = new ButtonStyle(null, null, null);

    @FunctionalInterface
    public interface ContentBuilder {
        Widget build(boolean active, Widget child);
    }
}
