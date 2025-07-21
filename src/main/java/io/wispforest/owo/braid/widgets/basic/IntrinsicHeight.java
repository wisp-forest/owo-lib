package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.OptionalDouble;

public class IntrinsicHeight extends SingleChildInstanceWidget {

    public IntrinsicHeight(Widget child) {
        super(child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<IntrinsicHeight> {

        public Instance(IntrinsicHeight widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var childSize = this.child.getIntrinsicHeight(constraints.maxWidth());

            var childConstraints = Constraints.of(
                constraints.minWidth(),
                childSize,
                constraints.maxWidth(),
                childSize
            ).respecting(constraints);

            this.transform.setSize(this.child.layout(childConstraints));
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.child.getIntrinsicWidth(height);
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.child.getIntrinsicHeight(width);
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.child.getBaselineOffset();
        }
    }
}
