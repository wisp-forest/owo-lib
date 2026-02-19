package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.core.Alignment;
import net.minecraft.util.Mth;

public class AlignmentLerp extends Lerp<Alignment> {

    public AlignmentLerp(Alignment start, Alignment end) {
        super(start, end);
    }

    @Override
    protected Alignment at(double t) {
        return Alignment.of(
            Mth.lerp(t, this.start.horizontal(), this.end.horizontal()),
            Mth.lerp(t, this.start.vertical(), this.end.vertical())
        );
    }
}
