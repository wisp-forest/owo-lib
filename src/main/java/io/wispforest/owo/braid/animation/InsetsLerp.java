package io.wispforest.owo.braid.animation;

import io.wispforest.owo.braid.core.Insets;
import net.minecraft.util.math.MathHelper;

public class InsetsLerp extends Lerp<Insets> {

    public InsetsLerp(Insets start, Insets end) {
        super(start, end);
    }

    @Override
    protected Insets at(double t) {
        return Insets.of(
            MathHelper.lerp(t, this.start.top(), this.end.top()),
            MathHelper.lerp(t, this.start.bottom(), this.end.bottom()),
            MathHelper.lerp(t, this.start.left(), this.end.left()),
            MathHelper.lerp(t, this.start.right(), this.end.right())
        );
    }
}
