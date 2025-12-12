package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.util.Mth;

import java.util.Objects;
import java.util.OptionalDouble;

public abstract class ConstraintWidget extends SingleChildInstanceWidget {

    protected ConstraintWidget(Widget child) {
        super(child);
    }

    protected abstract Constraints constraints();

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<ConstraintWidget> {

        public Instance(ConstraintWidget widget) {
            super(widget);
        }

        @Override
        public void setWidget(ConstraintWidget widget) {
            if (Objects.equals(this.widget.constraints(), widget.constraints())) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(this.widget.constraints().respecting(constraints), this.child);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return Mth.clamp(this.child.getIntrinsicWidth(height), this.widget.constraints().minWidth(), this.widget.constraints().maxWidth());
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return Mth.clamp(this.child.getIntrinsicHeight(width), this.widget.constraints().minHeight(), this.widget.constraints().maxHeight());
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.child.getBaselineOffset();
        }
    }
}
