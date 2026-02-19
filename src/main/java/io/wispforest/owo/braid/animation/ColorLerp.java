package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.core.Color;

public class ColorLerp extends Lerp<Color> {

    public ColorLerp(Color start, Color end) {
        super(start, end);
    }

    @Override
    protected Color at(double t) {
        return Color.mix(t, this.start, this.end);
    }
}
