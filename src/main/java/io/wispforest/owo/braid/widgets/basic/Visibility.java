package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.HitTestState;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.OptionalDouble;

public class Visibility extends SingleChildInstanceWidget {

    public final boolean visible;
    public final boolean reportSize;

    public Visibility(boolean visible, boolean reportSize, Widget child) {
        super(child);
        this.visible = visible;
        this.reportSize = reportSize;
    }

    public Visibility(boolean visible, Widget child) {
        this(visible, false, child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<Visibility> {

        public Instance(Visibility widget) {
            super(widget);
        }

        @Override
        public void setWidget(Visibility widget) {
            if (this.widget.visible == widget.visible
                && this.widget.reportSize == widget.reportSize) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var childSize = this.child.layout(constraints);
            if (this.widget.visible || this.widget.reportSize) {
                this.transform.setSize(childSize);
            } else {
                this.transform.setSize(Size.zero());
            }
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.widget.visible || this.widget.reportSize ? this.child.getIntrinsicWidth(height) : 0;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.widget.visible || this.widget.reportSize ? this.child.getIntrinsicHeight(width) : 0;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.widget.visible || this.widget.reportSize ? this.child.getBaselineOffset() : OptionalDouble.empty();
        }

        @Override
        public void draw(BraidGraphics graphics) {
            if (!this.widget.visible) return;
            super.draw(graphics);
        }

        @Override
        public void hitTest(double x, double y, HitTestState state) {
            if (!this.widget.visible) return;
            super.hitTest(x, y, state);
        }
    }
}
