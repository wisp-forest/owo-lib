package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class ScrollableWithBars extends StatefulWidget {

    public final @Nullable ScrollController horizontalController;
    public final @Nullable ScrollController verticalController;
    public final @Nullable ScrollAnimationSettings animationSettings;
    public final int scrollbarSize;
    public final BiFunction<LayoutAxis, ScrollController, Scrollbar> scrollbarFactory;
    public final Widget child;

    public ScrollableWithBars(@Nullable ScrollController horizontalController, @Nullable ScrollController verticalController, @Nullable ScrollAnimationSettings animationSettings, int scrollbarSize, BiFunction<LayoutAxis, ScrollController, Scrollbar> scrollbarFactory, Widget child) {
        this.horizontalController = horizontalController;
        this.verticalController = verticalController;
        this.animationSettings = animationSettings;
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
            this.horizontalController = newHorizontalController != null ? newHorizontalController : new ScrollController(this);

            var newVerticalController = this.widget().verticalController != null ? this.widget().verticalController : this.verticalController;
            this.verticalController = newVerticalController != null ? newVerticalController : new ScrollController(this);
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
            return new ListenableBuilder(
                this.horizontalController,
                horizontalContext -> {
                    var showHorizontalScrollbar = this.horizontalController.maxOffset() > 0;

                    return new ListenableBuilder(
                        this.verticalController,
                        verticalContext -> {
                            var showVerticalScrollbar = this.verticalController.maxOffset() > 0;

                            var widgets = new ArrayList<Widget>();
                            widgets.add(new StackBase(
                                new Padding(
                                    Insets.of(
                                        0,
                                        showHorizontalScrollbar ? this.widget().scrollbarSize : 0,
                                        0,
                                        showVerticalScrollbar ? this.widget().scrollbarSize : 0
                                    ),
                                    new Scrollable(
                                        true,
                                        true,
                                        this.horizontalController,
                                        this.verticalController,
                                        this.widget().animationSettings,
                                        this.widget().child
                                    )
                                )
                            ));

                            if (showVerticalScrollbar) {
                                widgets.add(new Align(
                                    Alignment.RIGHT,
                                    new Padding(
                                        showHorizontalScrollbar ? Insets.bottom(this.widget().scrollbarSize) : Insets.none(),
                                        new Sized(
                                            this.widget().scrollbarSize,
                                            null,
                                            this.widget().scrollbarFactory.apply(LayoutAxis.VERTICAL, this.verticalController)
                                        )
                                    )
                                ));
                            }

                            if (showHorizontalScrollbar) {
                                widgets.add(new Align(
                                    Alignment.BOTTOM,
                                    new Sized(
                                        null,
                                        this.widget().scrollbarSize,
                                        this.widget().scrollbarFactory.apply(LayoutAxis.HORIZONTAL, this.horizontalController)
                                    )
                                ));
                            }

                            return new Stack(widgets);
                        }
                    );
                }
            );
        }
    }
}
