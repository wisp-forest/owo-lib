package io.wispforest.owo.braid.widgets.drag;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;

import java.util.OptionalDouble;

public class DragArenaInstance extends MultiChildWidgetInstance<DragArena> {

    public DragArenaInstance(DragArena widget) {
        super(widget);
    }

    @Override
    public <W extends WidgetInstance<?>> W adopt(W child) {
        if (child != null && !(child.parentData instanceof DragParentData)) {
            child.parentData = new DragParentData(0, 0);
        }

        return super.adopt(child);
    }

    @Override
    public void doLayout(Constraints constraints) {
        for (var child : this.children) {
            child.layout(Constraints.unconstrained());

            var parentData = (DragParentData) child.parentData;
            child.transform.setX(parentData.x);
            child.transform.setY(parentData.y);
        }

        this.transform.setSize(constraints.maxSize());
    }

    @Override
    protected double measureIntrinsicWidth(double height) {
        return 0;
    }

    @Override
    protected double measureIntrinsicHeight(double width) {
        return 0;
    }

    @Override
    protected OptionalDouble measureBaselineOffset() {
        return this.computeHighestBaselineOffset();
    }
}
