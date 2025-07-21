package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.OptionalDouble;

public class Align extends SingleChildInstanceWidget {

    public final Alignment alignment;
    public final OptionalDouble widthFactor;
    public final OptionalDouble heightFactor;

    public Align(Alignment alignment, @Nullable Double widthFactor, @Nullable Double heightFactor, Widget child) {
        super(child);
        this.alignment = alignment;
        this.widthFactor = widthFactor != null ? OptionalDouble.of(widthFactor) : OptionalDouble.empty();
        this.heightFactor = heightFactor != null ? OptionalDouble.of(heightFactor) : OptionalDouble.empty();
    }

    public Align(Alignment alignment, Widget child) {
        this(alignment, null, null, child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<Align> {

        public Instance(Align widget) {
            super(widget);
        }

        @Override
        public void setWidget(Align widget) {
            if (Objects.equals(this.widget.widthFactor, widget.widthFactor)
                && Objects.equals(this.widget.heightFactor, widget.heightFactor)
                && Objects.equals(this.widget.alignment, widget.alignment)) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var widthFactor = this.widget.widthFactor;
            var heightFactor = this.widget.heightFactor;
            var alignment = this.widget.alignment;

            var childSize = this.child.layout(constraints.asLoose());
            var selfSize = Size.of(
                widthFactor.isPresent() || !constraints.hasBoundedWidth() ? childSize.width() * widthFactor.orElse(1) : constraints.maxWidth(),
                heightFactor.isPresent() || !constraints.hasBoundedHeight()
                    ? childSize.height() * heightFactor.orElse(1)
                    : constraints.maxHeight()
            ).constrained(constraints);

            var childX = alignment.alignHorizontal(selfSize.width(), childSize.width());
            var childY = alignment.alignVertical(selfSize.height(), childSize.height());
            this.child.transform.setX(childX);
            this.child.transform.setY(childY);

            this.transform.setSize(selfSize);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.child.getIntrinsicWidth(height) * (this.widget.widthFactor.orElse(1));
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.child.getIntrinsicHeight(width) * (this.widget.heightFactor.orElse(1));
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.child.getBaselineOffset().stream().map(operand -> operand + this.child.transform.y()).findAny();
        }
    }
}
