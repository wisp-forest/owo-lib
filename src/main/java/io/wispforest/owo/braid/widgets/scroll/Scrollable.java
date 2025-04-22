package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.CompoundListenable;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Clip;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Scrollable extends StatefulWidget {

    public final boolean horizontal;
    public final boolean vertical;
    public final @Nullable ScrollController horizontalController;
    public final @Nullable ScrollController verticalController;
    public final Widget child;

    public Scrollable(boolean horizontal, boolean vertical, @Nullable ScrollController horizontalController, @Nullable ScrollController verticalController, Widget child) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.horizontalController = horizontalController;
        this.verticalController = verticalController;
        this.child = child;
    }

    @Override
    public WidgetState<Scrollable> createState() {
        return new State();
    }

    public static class State extends WidgetState<Scrollable> {

        protected final CompoundListenable listenable = new CompoundListenable();

        protected ScrollController horizontalController;
        protected ScrollController verticalController;

        @Override
        public void init() {
            this.horizontalController = this.widget().horizontal ? Objects.requireNonNullElse(this.widget().horizontalController, new ScrollController()) : null;
            this.verticalController = this.widget().vertical ? Objects.requireNonNullElse(this.widget().verticalController, new ScrollController()) : null;

            if (this.horizontalController != null) this.listenable.addChild(this.horizontalController);
            if (this.verticalController != null) this.listenable.addChild(this.verticalController);
        }

        @Override
        public void didUpdateWidget(Scrollable oldWidget) {
            this.listenable.clear();

            if (this.widget().horizontal) {
                if (this.widget().horizontalController != null) {
                    this.horizontalController = this.widget().horizontalController;
                } else if (this.horizontalController == null || this.horizontalController == oldWidget.horizontalController) {
                    this.horizontalController = new ScrollController();
                }

                this.listenable.addChild(this.horizontalController);
            } else {
                this.horizontalController = null;
            }

            if (this.widget().vertical) {
                if (this.widget().verticalController != null) {
                    this.verticalController = this.widget().verticalController;
                } else if (this.verticalController == null || this.verticalController == oldWidget.verticalController) {
                    this.verticalController = new ScrollController();
                }

                this.listenable.addChild(this.verticalController);
            } else {
                this.verticalController = null;
            }
        }

        @Override
        public Widget build(BuildContext context) {
            return new Clip(
                new MouseArea(
                    widget -> widget
                        .scrollCallback((horizontal, vertical) -> {
                            if (Screen.hasShiftDown()) {
                                if (this.widget().horizontal) this.horizontalController.setOffset(this.horizontalController.offset() + vertical * -15);
                            } else {
                                if (this.widget().vertical) this.verticalController.setOffset(this.verticalController.offset() + vertical * -15);
                            }

                            if (this.widget().horizontal) this.horizontalController.setOffset(this.horizontalController.offset() + horizontal * -15);
                        }),
                    new ListenableBuilder(
                        this.listenable,
                        (innerContext, child) -> new RawScrollView(
                            this.horizontalController,
                            this.verticalController,
                            child
                        ),
                        this.widget().child
                    )
                )
            );
        }
    }
}
