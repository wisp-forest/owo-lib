package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
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

    public Sized(@Nullable Integer width, @Nullable Integer height, Widget child) {
        this(width == null ? null : width.doubleValue(), height == null ? null : height.doubleValue(), child);
    }

    public Sized(double width, double height, Widget child) {
        this((Double) width, (Double) height, child);
    }

    public Sized(Size size, Widget child) {
        this(size.width(), size.height(), child);
    }

    @Override
    protected Constraints constraints() {
        return Constraints.tightOnAxis(this.width, this.height);
    }
}
