package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class ScrollableWithBars extends StatefulWidget {

    public final @Nullable ScrollController horizontalController;
    public final @Nullable ScrollController verticalController;
    public final int scrollbarSize;
    public final BiFunction<LayoutAxis, ScrollController, Scrollbar> scrollbarFactory;
    public final Widget child;

    public ScrollableWithBars(@Nullable ScrollController horizontalController, @Nullable ScrollController verticalController, int scrollbarSize, BiFunction<LayoutAxis, ScrollController, Scrollbar> scrollbarFactory, Widget child) {
        this.horizontalController = horizontalController;
        this.verticalController = verticalController;
        this.scrollbarSize = scrollbarSize;
        this.scrollbarFactory = scrollbarFactory;
        this.child = child;
    }

    @Override
    public WidgetState<ScrollableWithBars> createState() {
        return new State();
    }

    public static class State extends WidgetState<ScrollableWithBars> {

        private ScrollController horizontalController;
        private ScrollController verticalController;

        private void updateControllers() {
            var newHorizontalController = this.widget().horizontalController != null ? this.widget().horizontalController : this.horizontalController;
            this.horizontalController = newHorizontalController != null ? newHorizontalController : new ScrollController();

            var newVerticalController = this.widget().verticalController != null ? this.widget().verticalController : this.verticalController;
            this.verticalController = newVerticalController != null ? newVerticalController : new ScrollController();
        }

        @Override
        public void init() {
            this.updateControllers();
        }

        @Override
        public void didUpdateWidget(ScrollableWithBars oldWidget) {
            this.updateControllers();
        }

        @Override
        public Widget build(BuildContext context) {
            return new Column(
                new Flexible(
                    new Row(
                        new Flexible(
                            new Scrollable(
                                true,
                                true,
                                this.horizontalController,
                                this.verticalController,
                                this.widget().child
                            )
                        ),
                        new Sized(
                            this.widget().scrollbarSize,
                            null,
                            this.widget().scrollbarFactory.apply(LayoutAxis.VERTICAL, this.verticalController)
                        )
                    )
                ),
                new Sized(
                    null,
                    this.widget().scrollbarSize,
                    this.widget().scrollbarFactory.apply(LayoutAxis.HORIZONTAL, this.horizontalController)
                )
            );
        }
    }
}
