package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class Incrementor extends StatelessWidget {

    public final @Nullable DoubleConsumer xCallback, yCallback;
    public final Widget child;

    public Incrementor(
        @Nullable DoubleConsumer xCallback,
        @Nullable DoubleConsumer yCallback,
        Widget child
    ) {
        this.xCallback = xCallback;
        this.yCallback = yCallback;
        this.child = child;
    }

    public Incrementor(@Nullable DoubleConsumer callback, Widget child) {
        this(callback, callback, child);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Actions(
            actions -> {
                if (this.xCallback != null) {
                    actions.addAction(ActionTrigger.RIGHT, () -> this.xCallback.accept(1));
                    actions.addAction(ActionTrigger.LEFT, () -> this.xCallback.accept(-1));
                    actions.addAction(ActionTrigger.HOME, () -> this.xCallback.accept(Double.NEGATIVE_INFINITY));
                    actions.addAction(ActionTrigger.END, () -> this.xCallback.accept(Double.POSITIVE_INFINITY));
                }
                if (this.yCallback != null) {
                    actions.addAction(ActionTrigger.UP, () -> this.yCallback.accept(1));
                    actions.addAction(ActionTrigger.DOWN, () -> this.yCallback.accept(-1));
                    actions.addAction(ActionTrigger.PAGE_UP, () -> this.yCallback.accept(Double.POSITIVE_INFINITY));
                    actions.addAction(ActionTrigger.PAGE_DOWN, () -> this.yCallback.accept(Double.NEGATIVE_INFINITY));
                }
            },
            new MouseArea(
                mouseArea -> mouseArea
                    .scrollCallback((baseHorizontal, baseVertical) -> {
                        var handled = false;
                        var horizontal = Screen.hasShiftDown() ? baseVertical : baseHorizontal;
                        var vertical = Screen.hasShiftDown() ? baseHorizontal : baseVertical;
                        if (horizontal != 0 && this.xCallback != null) {
                            this.xCallback.accept(horizontal);
                            handled = true;
                        }
                        if (vertical != 0 && this.yCallback != null) {
                            this.yCallback.accept(vertical);
                            handled = true;
                        }
                        return handled;
                    }),
                this.child
            )
        );
    }
}
