package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.OptionalDouble;

public class AspectRatio extends SingleChildInstanceWidget {

    public final double ratio;

    public AspectRatio(double ratio, Widget child) {
        super(child);
        this.ratio = ratio;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    // ---

    public static Size applyAspectRatioToMaxSize(Constraints constraints, double ratio) {
        double width = constraints.maxWidth();
        double height;

        if (Double.isFinite(width)) {
            height = width / ratio;
        } else {
            height = constraints.maxHeight();
            width = height * ratio;
        }

        return applyAspectRatio(constraints, Size.of(width, height));
    }

    public static Size applyAspectRatio(Constraints constraints, Size size) {
        if (constraints.isTight()) {
            return constraints.minSize();
        }

        var width = size.width();
        var height = size.height();
        var ratio = width / height;

        if (width > constraints.maxWidth()) {
            width = constraints.maxWidth();
            height = width / ratio;
        }

        if (height > constraints.maxHeight()) {
            height = constraints.maxHeight();
            width = height * ratio;
        }

        if (width < constraints.minWidth()) {
            width = constraints.minWidth();
            height = width / ratio;
        }

        if (height < constraints.minHeight()) {
            height = constraints.minHeight();
            width = height * ratio;
        }

        return Size.of(width, height).constrained(constraints);
    }

    // ---

    public static class Instance extends SingleChildWidgetInstance<AspectRatio> {

        public Instance(AspectRatio widget) {
            super(widget);
        }

        @Override
        public void setWidget(AspectRatio widget) {
            if (widget.ratio == this.widget.ratio) return;

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var size = AspectRatio.applyAspectRatioToMaxSize(constraints, this.widget.ratio);
            this.transform.setSize(size);

            this.child.layout(Constraints.tight(size));
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return Double.isFinite(height) ? height * this.widget.ratio : this.child.getIntrinsicWidth(height);
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return Double.isFinite(width) ? width / this.widget.ratio : this.child.getIntrinsicHeight(width);
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.child.getBaselineOffset();
        }
    }
}
