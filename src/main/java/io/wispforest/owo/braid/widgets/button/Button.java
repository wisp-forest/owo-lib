package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import org.jetbrains.annotations.Nullable;

/// A Vanilla-styled [RawButton]
/// @see RawButton
public class Button extends StatelessWidget {

    /// The callback to invoke when this widget is clicked.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable Runnable onClick;
    public final Widget child;

    public Button(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    /// Create a `Button` with an explicit active state
    public Button(Runnable onClick, boolean active, Widget child) {
        this(active ? onClick : null, child);
    }

    @Override
    public Widget build(BuildContext context) {
        var disabled = this.onClick == null || ControlsOverride.controlsDisabled(context);
        var content = new ButtonPanel(!disabled, this.child);
        if (disabled) return content;
        return new RawButton(this.onClick, content);
    }
}
