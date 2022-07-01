package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.definitions.*;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;

public class HorizontalFlowLayout extends FlowLayout {

    protected HorizontalFlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);
    }

    @Override
    public void layout(Size space) {
        var layoutWidth = new MutableInt(0);
        var layoutHeight = new MutableInt(0);

        final var layout = new ArrayList<Component>();
        final var padding = this.padding.get();
        final var childSpace = this.calculateChildSpace(space);

        for (int i = children.size() - 1; i >= 0; i--) {
            final var child = this.children.get(i);

            this.mountChild(child, childSpace, c -> {
                layout.add(child);

                child.inflate(childSpace);
                child.mount(this,
                        this.x + padding.left() + child.margins().get().left() + layoutWidth.intValue(),
                        this.y + padding.top() + child.margins().get().top());

                final var childSize = child.fullSize();
                layoutWidth.add(childSize.width());
                if (childSize.height() > layoutHeight.intValue()) {
                    layoutHeight.setValue(childSize.height());
                }
            });
        }

        this.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
        if (this.horizontalSizing.get().method == Sizing.Method.CONTENT) {
            this.applyHorizontalContentSizing(this.horizontalSizing.get());
        }

        if (this.verticalSizing.get().method == Sizing.Method.CONTENT) {
            this.applyVerticalContentSizing(this.verticalSizing.get());
        }

        if (this.verticalAlignment() != VerticalAlignment.TOP) {
            for (var component : layout) {
                component.setY(component.y() + this.verticalAlignment().align(component.fullSize().height(), this.height - padding.vertical()));
            }
        }

        if (this.horizontalAlignment() != HorizontalAlignment.LEFT) {
            for (var component : layout) {
                if (this.horizontalAlignment() == HorizontalAlignment.CENTER) {
                    component.setX(component.x() + (this.width - padding.horizontal() - layoutWidth.intValue()) / 2);
                } else {
                    component.setX(component.x() + (this.width - padding.horizontal() - layoutWidth.intValue()));
                }
            }
        }
    }
}
