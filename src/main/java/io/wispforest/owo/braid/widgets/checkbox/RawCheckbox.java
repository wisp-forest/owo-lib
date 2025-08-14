package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.button.RawButton;
import io.wispforest.owo.braid.widgets.stack.Stack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/// A [RawButton] that toggles between two states, checked and unchecked.<br>
/// When toggled, invokes [#onUpdate] with the new [#checked] state
///
/// If no [#onUpdate] is provided, this widget is inactive`
///
/// @see RawButton
public class RawCheckbox extends StatelessWidget {

    public final boolean checked;
    /// The callback to invoke when this widget is toggled.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable CheckboxCallback onUpdate;
    public final @Nullable Widget background;
    public final Widget checkmark;

    public RawCheckbox(
        boolean checked,
        @Nullable CheckboxCallback onUpdate,
        @Nullable Widget background,
        Widget checkmark
    ) {
        this.checked = checked;
        this.onUpdate = onUpdate;
        this.background = background;
        this.checkmark = checkmark;
    }

    /// Create a `RawCheckbox` with an explicit active state
    public RawCheckbox(
        boolean checked,
        CheckboxCallback onUpdate,
        boolean active,
        @Nullable Widget background,
        Widget checkmark
    ) {
        this(checked, active ? onUpdate : null, background, checkmark);
    }

    /// Create a `RawCheckbox` with no background
    public RawCheckbox(
        boolean checked,
        @Nullable CheckboxCallback onUpdate,
        Widget checkmark
    ) {
        this(checked, onUpdate, null, checkmark);
    }

    /// Create a `RawCheckbox` with no background and an explicit active state
    public RawCheckbox(
        boolean checked,
        CheckboxCallback onUpdate,
        boolean active,
        Widget checkmark
    ) {
        this(checked, active ? onUpdate : null, null, checkmark);
    }

    @Override
    public Widget build(BuildContext context) {
        var content = this.background == null ? this.checkmark
            : this.checked ? new Stack(this.background, this.checkmark)
                : this.background;
        if (this.onUpdate == null) return content;
        return new RawButton(
            () -> this.onUpdate.accept(!this.checked),
            content
        );
    }

    /// A specialized [Consumer] for checkbox state changes
    ///
    /// @apiNote This exists purely to get IDEs to autofill the lambda parameter name
    @FunctionalInterface
    public interface CheckboxCallback extends Consumer<Boolean> {
        void accept(Boolean nowChecked);
    }
}
