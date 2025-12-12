package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetTransform;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;

import java.util.OptionalDouble;

public class CustomDraw extends LeafInstanceWidget {

    public final CustomDrawFunction function;

    public CustomDraw(CustomDrawFunction function) {
        this.function = function;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @FunctionalInterface
    public interface CustomDrawFunction {
        void draw(BraidGraphics graphics, WidgetTransform transform);
    }

    public static class Instance extends LeafWidgetInstance<CustomDraw> {

        public Instance(CustomDraw widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var size = constraints.minSize();
            this.transform.setSize(size);
        }

        @Override
        public void draw(BraidGraphics graphics) {
            this.widget.function.draw(graphics, this.transform);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return 0;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return 0;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }
    }
}
