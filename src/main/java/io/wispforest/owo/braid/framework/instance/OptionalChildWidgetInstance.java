package io.wispforest.owo.braid.framework.instance;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

public abstract class OptionalChildWidgetInstance<T extends InstanceWidget> extends WidgetInstance<T> {

    protected @Nullable WidgetInstance<?> child;

    public OptionalChildWidgetInstance(T widget) {
        super(widget);
    }

    @Override
    public void draw(BraidGraphics graphics) {
        if (this.child != null) {
            this.drawChild(graphics, this.child);
        }
    }

    @Override
    public void visitChildren(Visitor visitor) {
        if (this.child != null) {
            visitor.visit(this.child);
        }
    }

    public WidgetInstance<?> child() {
        Preconditions.checkNotNull(this.child, "tried to retrieve child of SingleChildWidgetInstance before it was set");
        return this.child;
    }

    public void setChild(@Nullable WidgetInstance<?> value) {
        if (value == this.child) return;

        this.child = this.adopt(value);
        this.markNeedsLayout();
    }

    public static abstract class ShrinkWrap<T extends InstanceWidget> extends OptionalChildWidgetInstance<T> {

        public ShrinkWrap(T widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(constraints, this.child);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.child != null ? this.child.getIntrinsicWidth(height) : 0;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.child != null ? this.child.getIntrinsicHeight(width) : 0;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.child != null ? this.child.getBaselineOffset() : OptionalDouble.empty();
        }
    }
}
