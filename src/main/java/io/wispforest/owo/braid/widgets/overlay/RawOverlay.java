package io.wispforest.owo.braid.widgets.overlay;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

public class RawOverlay extends MultiChildInstanceWidget {

    public RawOverlay(List<? extends RawOverlayElement> children) {
        super(children);
    }

    public RawOverlay(RawOverlayElement... children) {
        this(Arrays.asList(children));
    }

    @Override
    public MultiChildWidgetInstance<RawOverlay> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends MultiChildWidgetInstance<RawOverlay> {

        public Instance(RawOverlay widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            for (var child : this.children) {
                child.layout(Constraints.unconstrained());

                var parentData = (OverlayParentData) child.parentData;
                child.transform.setX(parentData.x);
                child.transform.setY(parentData.y);
            }

            this.transform.setSize(constraints.maxFiniteOrMinSize());
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
            return OptionalDouble.empty();
        }
    }
}
