package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.ui.util.UISounds;
import org.jetbrains.annotations.Nullable;

/// A [Widget] that allows its child to be clicked
///
/// @author glico
/// @author chyzman
public class RawButton extends StatelessWidget {

    /// The Runnable Called when this [RawButton] is clicked
    ///
    /// If `null`, disables this [RawButton]
    public final @Nullable Runnable onClick;
    /// This [Widget]'s child
    public final Widget child;

    /// Create a new [RawButton]
    ///
    /// If [#onClick] is `null`, this [RawButton] will be disabled
    public RawButton(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    /// Create a new [RawButton] with an explicit active state
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
