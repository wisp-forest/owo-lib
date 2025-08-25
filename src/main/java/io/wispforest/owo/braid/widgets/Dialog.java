package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.HitTestTrap;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.basic.action.Trigger;
import io.wispforest.owo.ui.core.Color;
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
            new Actions(
                widget -> {
                    if (this.barrierCanDismiss) widget.addAction(DISMISS_TRIGGER, () -> Navigator.pop(context));
                },
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

    private static final Color DEFAULT_BARRIER_COLOR = new Color(0, 0, 0, .25f);
    private static final ActionTrigger DISMISS_TRIGGER = new ActionTrigger(Trigger.ofMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT));
}