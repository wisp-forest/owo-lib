package io.wispforest.owo.braid.core;

import com.google.common.base.Preconditions;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.framework.BuildContext;
import org.joml.Vector2d;
import org.joml.Vector2f;

public record RelativePosition(BuildContext context, double x, double y) {

    public Vector2d convertTo(BuildContext ancestor) {
        var contextInstance = context.instance();
        var ancestorInstance = ancestor.instance();

        if (Owo.DEBUG) {
            Preconditions.checkArgument(
                contextInstance.ancestors().contains(ancestorInstance),
                "a RelativePosition can only be converted to the coordinate system of an ancestor"
            );
        }

        var coordinates = new Vector2f((float) this.x, (float) this.y);
        contextInstance.computeTransformFrom(ancestorInstance).invert().transformPosition(coordinates);

        return new Vector2d(coordinates.x, coordinates.y);
    }
}
