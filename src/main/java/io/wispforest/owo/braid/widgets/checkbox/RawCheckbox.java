package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import io.wispforest.owo.braid.widgets.button.RawButton;
import io.wispforest.owo.braid.widgets.stack.Stack;
import org.jetbrains.annotations.Nullable;

public class RawCheckbox extends StatelessWidget {

    public final boolean checked;
    public final @Nullable CheckboxCallback onUpdate;
    public final Widget background;
    public final Widget checkmark;

    public RawCheckbox(boolean checked, @Nullable CheckboxCallback onUpdate, Widget background, Widget checkmark) {
        this.checked = checked;
        this.onUpdate = onUpdate;
        this.background = background;
        this.checkmark = checkmark;
    }

    public RawCheckbox(boolean checked, CheckboxCallback onUpdate, boolean active, Widget background, Widget checkmark) {
        this(checked, active ? onUpdate : null, background, checkmark);
    }

    @Override
    public Widget build(BuildContext context) {
        var content = this.checked ? new Stack(this.background, this.checkmark) : this.background;
        var disabled = this.onUpdate == null || ControlsOverride.controlsDisabled(context);
        if (disabled) return content;
        //TODO: should disabled be passed to background and checkmark?
        return new RawButton(
            () -> this.onUpdate.accept(!this.checked),
            content
        );
    }

    @FunctionalInterface
    public interface CheckboxCallback {
        void accept(boolean nowChecked);
    }
}
