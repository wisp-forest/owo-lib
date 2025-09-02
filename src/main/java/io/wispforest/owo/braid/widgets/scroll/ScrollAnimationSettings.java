package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.animation.Easing;

import java.time.Duration;

public record ScrollAnimationSettings(Duration duration, Easing easing) {
    public static final ScrollAnimationSettings DEFAULT = new ScrollAnimationSettings(Duration.ofMillis(250), Easing.OUT_QUART);
    public static final ScrollAnimationSettings NO_ANIMATION = new ScrollAnimationSettings(null, null);
}
