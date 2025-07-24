package io.wispforest.owo.braid.animation;

import io.wispforest.owo.ui.core.Color;

public class ColorLerp extends Lerp<Color> {

    public ColorLerp(Color start, Color end) {
        super(start, end);
    }

    @Override
    protected Color at(double t) {
        return this.start.interpolate(this.end, (float) t);
    }
}
