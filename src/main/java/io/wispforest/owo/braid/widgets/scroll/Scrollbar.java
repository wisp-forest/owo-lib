package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.slider.RawSlider;
import org.jetbrains.annotations.Nullable;

public class Scrollbar extends StatelessWidget {

    public final LayoutAxis axis;
    public final ScrollController controller;
    public final @Nullable Widget track;
    public final Widget handle;

    public Scrollbar(LayoutAxis axis, ScrollController controller, @Nullable Widget track, Widget handle) {
        this.axis = axis;
        this.controller = controller;
        this.track = track;
        this.handle = handle;
    }

    @Override
    public Widget build(BuildContext context) {
        return new ListenableBuilder(
            this.controller,
            buildContext -> {
                return new LayoutBuilder(
                    (ctx, constraints) -> {
                        var currentOffset = this.controller.offset;
                        var maxOffset = this.controller.maxOffset;

                        var selfSize = constraints.maxOnAxis(this.axis);
                        var childSize = selfSize + maxOffset;
                        var scrollbarLength = Math.min((selfSize / childSize) * selfSize, selfSize);

                        return maxOffset != 0 ? new RawSlider(
                            currentOffset,
                            this.axis.choose(0, maxOffset).doubleValue(),
                            this.axis.choose(maxOffset, 0).doubleValue(),
                            null,
                            this.axis,
                            this.controller::setOffset,
                            this.track,
                            this.handle,
                            Math.max(5, scrollbarLength)
                        ) : new Padding(Insets.none());
                    }
                );
            }
        );
    }
}
