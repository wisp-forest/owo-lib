package io.wispforest.owo.braid.widgets.slider.range;

import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RangeSliderStyle(
    @Nullable Widget track,
    @Nullable Widget rangeIndicator,
    @Nullable HandleBuilder minHandleBuilder,
    @Nullable Double minHandleSize,
    @Nullable HandleBuilder maxHandleBuilder,
    @Nullable Double maxHandleSize,
    @Nullable Optional<SoundEvent> confirmSound
) {
    public RangeSliderStyle overriding(RangeSliderStyle other) {
        //noinspection OptionalAssignedToNull
        return new RangeSliderStyle(
            this.track != null ? this.track : other.track,
            this.rangeIndicator != null ? this.rangeIndicator : other.rangeIndicator,
            this.minHandleBuilder != null ? this.minHandleBuilder : other.minHandleBuilder,
            this.minHandleSize != null ? this.minHandleSize : other.minHandleSize,
            this.maxHandleBuilder != null ? this.maxHandleBuilder : other.maxHandleBuilder,
            this.maxHandleSize != null ? this.maxHandleSize : other.maxHandleSize,
            this.confirmSound != null ? this.confirmSound : other.confirmSound
        );
    }

    public static final RangeSliderStyle DEFAULT = new RangeSliderStyle(null, null, null, null, null, null, null);

    @FunctionalInterface
    public interface HandleBuilder {
        Widget build(boolean active);
    }
}
