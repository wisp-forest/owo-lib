package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.OptionalDouble;

public class Padding extends OptionalChildInstanceWidget {

    public final Insets insets;

    public Padding(Insets insets, @Nullable Widget child) {
        super(child);
        this.insets = insets;
    }

    public Padding(Insets insets) {
        this(insets, null);
    }

    public Padding(Size size) {
        this(Insets.right(size.width()).withTop(size.height()), null);
    }


    @Override
    public OptionalChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends OptionalChildWidgetInstance<Padding> {

        public Instance(Padding widget) {
            super(widget);
        }

        @Override
        public void setWidget(Padding widget) {
            if (Objects.equals(this.widget.insets, widget.insets)) return;

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var insets = this.widget.insets;
            var childConstraints = Constraints.of(
                Math.max(0, constraints.minWidth() - insets.horizontal()),
                Math.max(0, constraints.minHeight() - insets.vertical()),
                Math.max(0, constraints.maxWidth() - insets.horizontal()),
                Math.max(0, constraints.maxHeight() - insets.vertical())
            );

            var size = (this.child != null ? this.child.layout(childConstraints) : Size.zero()).withInsets(insets).constrained(constraints);
            this.transform.setSize(size);

            if (this.child != null) {
                this.child.transform.setX(insets.left());
                this.child.transform.setY(insets.top());
            }
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            var childWidth = this.child != null ? this.child.getIntrinsicWidth(height) : 0;
            return childWidth + this.widget.insets.horizontal();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            var childHeight = this.child != null ? this.child.getIntrinsicHeight(width) : 0;
            return childHeight + this.widget.insets.vertical();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            var childBaseline = this.child != null ? this.child.getBaselineOffset() : OptionalDouble.empty();
            if (childBaseline.isEmpty()) return OptionalDouble.empty();

            return OptionalDouble.of(childBaseline.getAsDouble() + this.widget.insets.top());
        }
    }
}
