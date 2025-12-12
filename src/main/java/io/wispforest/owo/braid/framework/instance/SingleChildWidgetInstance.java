package io.wispforest.owo.braid.framework.instance;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;

import java.util.OptionalDouble;

public abstract class SingleChildWidgetInstance<T extends InstanceWidget> extends WidgetInstance<T> {

    protected WidgetInstance<?> child;

    public SingleChildWidgetInstance(T widget) {
        super(widget);
    }

    @Override
    public void draw(BraidGraphics graphics) {
        this.drawChild(graphics, this.child);
    }

    @Override
    public void visitChildren(Visitor visitor) {
        visitor.visit(this.child);
    }

    public WidgetInstance<?> child() {
        Preconditions.checkNotNull(this.child, "tried to retrieve child of SingleChildWidgetInstance before it was set");
        return this.child;
    }

    public void setChild(WidgetInstance<?> value) {
        if (value == this.child) return;

        this.child = this.adopt(value);
        this.markNeedsLayout();
    }

    public static abstract class ShrinkWrap<T extends InstanceWidget> extends SingleChildWidgetInstance<T> {

        public ShrinkWrap(T widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(constraints, this.child);
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
