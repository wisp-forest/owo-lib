package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.ui.util.UISounds;
import org.jetbrains.annotations.Nullable;

/// A low-level [Widget] that allows [#child] to be focused and clicked.<br>
/// When active and hovered, the cursor uses [CursorStyle#HAND].
///
/// If no [#onClick] is provided, this widget is inactive
/// and falls through to its child
public class RawButton extends StatelessWidget {
    /// The callback to invoke when this widget is clicked.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable Runnable onClick;
    public final Widget child;


    public RawButton(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    /// Create a `RawButton` with an explicit active state
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
