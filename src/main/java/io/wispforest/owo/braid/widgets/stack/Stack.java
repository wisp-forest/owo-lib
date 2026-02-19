package io.wispforest.owo.braid.widgets.stack;

import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

public class Stack extends MultiChildInstanceWidget {

    public final Alignment alignment;

    public Stack(Alignment alignment, List<? extends Widget> children) {
        super(children);
        this.alignment = alignment;
    }

    public Stack(List<? extends Widget> children) {
        this(Alignment.CENTER, children);
    }

    public Stack(Alignment alignment, Widget... children) {
        this(alignment, Arrays.asList(children));
    }

    public Stack(Widget... children) {
        this(Alignment.CENTER, children);
    }

    @Override
    public MultiChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends MultiChildWidgetInstance<Stack> {

        public Instance(Stack widget) {
            super(widget);
        }

        @Override
        public void setWidget(Stack widget) {
            if (this.widget.alignment == widget.alignment) return;

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var sizingBase = this.children.stream().filter(child -> child.parentData == StackParentData.INSTANCE).findFirst().orElse(null);

            Size selfSize;
            if (sizingBase != null) {
                selfSize = sizingBase.layout(constraints);

                var childConstraints = Constraints.tight(selfSize);
                for (var child : Iterables.filter(this.children, child -> child != sizingBase)) {
                    child.layout(childConstraints);
                }
            } else {
                selfSize = BraidUtils.fold(this.children, Size.zero(), (size, child) -> Size.max(size, child.layout(constraints)));
            }

            for (var child : this.children) {
                child.transform.setX(
                    this.widget.alignment.alignHorizontal(selfSize.width(), child.transform.width())
                );
                child.transform.setY(
                    this.widget.alignment.alignVertical(selfSize.height(), child.transform.height())
                );
            }

            this.transform.setSize(selfSize);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return BraidUtils.fold(
                this.children,
                0.0,
                (width, child) -> Math.max(child.getIntrinsicWidth(height), width)
            );
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return BraidUtils.fold(
                this.children,
                0.0,
                (height, child) -> Math.max(child.getIntrinsicHeight(width), height)
            );
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.computeHighestBaselineOffset();
        }
    }
}
