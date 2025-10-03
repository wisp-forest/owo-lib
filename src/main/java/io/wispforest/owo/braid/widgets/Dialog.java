package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.HitTestTrap;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import org.lwjgl.glfw.GLFW;

public class Dialog extends StatelessWidget {

    public final Color barrierColor;
    public final boolean barrierCanDismiss;
    public final Widget child;

    public Dialog(Color barrierColor, boolean barrierCanDismiss, Widget child) {
        this.barrierColor = barrierColor;
        this.barrierCanDismiss = barrierCanDismiss;
        this.child = child;
    }

    public Dialog(Color barrierColor, Widget child) {
        this(barrierColor, true, child);
    }

    public Dialog(boolean barrierCanDismiss, Widget child) {
        this(DEFAULT_BARRIER_COLOR, barrierCanDismiss, child);
    }

    public Dialog(Widget child) {
        this(DEFAULT_BARRIER_COLOR, true, child);
    }

    @Override
    public Widget build(BuildContext context) {
        return new HitTestTrap(
            new MouseArea(
                widget -> widget
                    .clickCallback((x, y, button, modifiers) -> {
                        if (!this.barrierCanDismiss || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                        Navigator.pop(context);
                        return true;
                    }),
                new Box(
                    this.barrierColor,
                    new Center(
                        new HitTestTrap(
                            this.child
                        )
                    )
                )
            )
        );
    }

    // ---

    private static final Color DEFAULT_BARRIER_COLOR = Color.BLACK.withA(.25);
}