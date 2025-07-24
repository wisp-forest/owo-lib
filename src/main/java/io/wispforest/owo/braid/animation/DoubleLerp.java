package io.wispforest.owo.braid.animation;

import net.minecraft.util.math.MathHelper;

public class DoubleLerp extends Lerp<Double> {

    public DoubleLerp(Double start, Double end) {
        super(start, end);
    }

    @Override
    protected Double at(double t) {
        return MathHelper.lerp(t, this.start, this.end);
    }
}
