package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.core.Insets;
import net.minecraft.util.Mth;

public class InsetsLerp extends Lerp<Insets> {

    public InsetsLerp(Insets start, Insets end) {
        super(start, end);
    }

    @Override
    protected Insets at(double t) {
        return Insets.of(
            Mth.lerp(t, this.start.top(), this.end.top()),
            Mth.lerp(t, this.start.bottom(), this.end.bottom()),
            Mth.lerp(t, this.start.left(), this.end.left()),
            Mth.lerp(t, this.start.right(), this.end.right())
        );
    }
}
