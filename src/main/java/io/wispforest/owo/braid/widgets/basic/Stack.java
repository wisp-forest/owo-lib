package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Arrays;
import java.util.List;

public class Stack extends MultiChildInstanceWidget {

    public final Alignment alignment;

    public Stack(Alignment alignment, List<Widget> children) {
        super(children);
        this.alignment = alignment;
    }

    public Stack( List<Widget> children) {
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
        protected void doLayout(Constraints constraints) {
            var maxSize = BraidUtils.fold(this.children, Size.zero(), (size, child) -> size = Size.max(size, child.layout(constraints)));

            for (var child : children) {
                child.transform.setX(
                    this.widget.alignment.alignHorizontal(maxSize.width(), child.transform.width())
                );
                child.transform.setY(
                    this.widget.alignment.alignHorizontal(maxSize.height(), child.transform.height())
                );
            }

            this.transform.setSize(maxSize);
        }
    }
}
