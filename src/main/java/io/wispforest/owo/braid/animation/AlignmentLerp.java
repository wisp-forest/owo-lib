package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.core.Alignment;
import net.minecraft.util.math.MathHelper;

public class AlignmentLerp extends Lerp<Alignment> {

    public AlignmentLerp(Alignment start, Alignment end) {
        super(start, end);
    }

    @Override
    protected Alignment at(double t) {
        return Alignment.of(
            MathHelper.lerp(t, this.start.horizontal(), this.end.horizontal()),
            MathHelper.lerp(t, this.start.vertical(), this.end.vertical())
        );
    }
}
