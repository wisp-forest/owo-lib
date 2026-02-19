package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.CustomWidgetTransform;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetTransform;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.joml.Matrix3x2f;

import java.util.OptionalDouble;

public class RotatedLayout extends SingleChildInstanceWidget {

    public final int increments;

    public RotatedLayout(int increments, Widget child) {
        super(child);
        this.increments = increments;
    }

    @Override
    public SingleChildWidgetInstance<RotatedLayout> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<RotatedLayout> {

        public Instance(RotatedLayout widget) {
            super(widget);
            this.visualIncrements = Math.floorMod(widget.increments, 4);
        }

        @Override
        protected WidgetTransform createTransform() {
            var transform = new CustomWidgetTransform();
            transform.setApplyAtCenter(false);

            return transform;
        }

        private int visualIncrements;

        private boolean isVertical() {
            return this.visualIncrements % 2 == 1;
        }

        @Override
        public void setWidget(RotatedLayout widget) {
            if (this.visualIncrements == Math.floorMod(widget.increments, 4)) {
                return;
            }

            super.setWidget(widget);

            this.visualIncrements = Math.floorMod(widget.increments, 4);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var isVertical = this.isVertical();
            var childConstraints = isVertical
                ? Constraints.of(constraints.minHeight(), constraints.minWidth(), constraints.maxHeight(), constraints.maxWidth())
                : constraints;

            var childSize = this.child.layout(childConstraints);
            var selfSize = isVertical
                ? Size.of(childSize.height(), childSize.width())
                : childSize;

            this.transform.setSize(selfSize);

            var childTransform = new Matrix3x2f()
                .translate((float) (selfSize.width() / 2), (float) (selfSize.height() / 2))
                .rotate((float) (this.visualIncrements * Math.PI / 2))
                .translate((float) (-childSize.width() / 2), (float) (-childSize.height() / 2));

            ((CustomWidgetTransform) this.transform).setMatrix(childTransform);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.isVertical() ? this.child.getIntrinsicHeight(height) : this.child.getIntrinsicWidth(height);
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.isVertical() ? this.child.getIntrinsicWidth(width) : this.child.getIntrinsicHeight(width);
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }
    }
}
