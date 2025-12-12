package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SliderStyle<HandleSize>(
    @Nullable Widget track,
    @Nullable HandleBuilder handleBuilder,
    @Nullable HandleSize handleSize,
    @Nullable Optional<SoundEvent> confirmSound
) {
    public SliderStyle<HandleSize> overriding(SliderStyle<HandleSize> other) {
        //noinspection OptionalAssignedToNull
        return new SliderStyle<>(
            this.track != null ? this.track : other.track,
            this.handleBuilder != null ? this.handleBuilder : other.handleBuilder,
            this.handleSize != null ? this.handleSize : other.handleSize,
            this.confirmSound != null ? this.confirmSound : other.confirmSound
        );
    }

    private static final SliderStyle<?> DEFAULT = new SliderStyle<>(null, null, null, null);
    public static <HandleSize> SliderStyle<HandleSize> getDefault() {
        //noinspection unchecked
        return (SliderStyle<HandleSize>) DEFAULT;
    }

    @FunctionalInterface
    public interface HandleBuilder {
        Widget build(boolean active);
    }
}
