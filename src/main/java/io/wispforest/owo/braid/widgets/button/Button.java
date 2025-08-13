package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

/// A Vanilla-styled [RawButton] with an arbitrary child [Widget]
/// @see RawButton
///
/// @author glico
/// @author chyzman
public class Button extends StatelessWidget {

    /// The [Runnable] called when this [Button] is clicked
    ///
    /// /// If `null`, disables this [Button]
    public final @Nullable Runnable onClick;
    /// This [Widget]'s child
    public final Widget child;

    /// Create a new [Button]
    ///
    /// If [#onClick] is `null`, this [Button] will be disabled
    public Button(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    /// Create a new [Button] with an explicit active state
    public Button(Runnable onClick, boolean active, Widget child) {
        this(active ? onClick : null, child);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawButton(this.onClick, new ButtonPanel(this.onClick != null, this.child));
    }
}
