package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.ui.util.UISounds;
import org.jetbrains.annotations.Nullable;

public class RawButton extends StatelessWidget {

    public final @Nullable Runnable onClick;
    public final Widget child;

    public RawButton(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    public RawButton(Runnable onClick, boolean active, Widget child) {
        this(active ? onClick : null, child);
    }

    @Override
    public Widget build(BuildContext context) {
        if (this.onClick == null) return this.child;
        return Actions.click(
            widget -> widget.cursorStyle(CursorStyle.HAND),
            () -> {
                this.onClick.run();
                UISounds.playButtonSound();
            },
            child
        );
    }
}
