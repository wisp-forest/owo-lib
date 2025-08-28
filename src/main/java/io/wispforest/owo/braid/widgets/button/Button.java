package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import org.jetbrains.annotations.Nullable;

public class Button extends StatelessWidget {

    public final @Nullable Runnable onClick;
    public final Widget child;

    public Button(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    public Button(Runnable onClick, boolean active, Widget child) {
        this(active ? onClick : null, child);
    }

    @Override
    public Widget build(BuildContext context) {
        var disabled = this.onClick == null || ControlsOverride.controlsDisabled(context);
        var content = new ButtonPanel(!disabled, this.child);


        // the second newline that should be above this comment is there to piss glisco off
         return !disabled ? new RawButton(this.onClick, content) : content;
    }
}
