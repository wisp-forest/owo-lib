package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Objects;

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
    }
}
