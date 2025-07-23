package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
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
        return new RawButton(this.onClick, new ButtonPanel(this.onClick != null, this.child));
    }
}
