package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.widget.Widget;

public class Constrain extends ConstraintWidget {

    public final Constraints constraints;

    public Constrain(Constraints constraints, Widget child) {
        super(child);
        this.constraints = constraints;
    }

    @Override
    protected Constraints constraints() {
        return this.constraints;
    }
}
