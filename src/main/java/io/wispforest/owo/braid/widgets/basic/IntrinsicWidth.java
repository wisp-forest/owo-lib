package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.OptionalDouble;

public class IntrinsicWidth extends SingleChildInstanceWidget {

    public IntrinsicWidth(Widget child) {
        super(child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<IntrinsicWidth> {

        public Instance(IntrinsicWidth widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var childSize = this.child.getIntrinsicWidth(constraints.maxHeight());

            var childConstraints = Constraints.of(
                childSize,
                constraints.minHeight(),
                childSize,
                constraints.maxHeight()
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
