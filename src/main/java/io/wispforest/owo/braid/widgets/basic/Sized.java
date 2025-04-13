package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class Sized extends ConstraintWidget {

    public final @Nullable Double width;
    public final @Nullable Double height;

    public Sized(@Nullable Double width, @Nullable Double height, Widget child) {
        super(child);
        this.width = width;
        this.height = height;
    }

    @Override
    protected Constraints constraints() {
        return Constraints.tightOnAxis(this.width, this.height);
    }
}
