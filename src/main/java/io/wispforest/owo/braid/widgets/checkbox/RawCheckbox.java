package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.button.RawButton;
import io.wispforest.owo.braid.widgets.stack.Stack;

import java.util.function.Consumer;

public class RawCheckbox extends StatelessWidget {

    public final boolean checked;
    public final Consumer<Boolean> onUpdate;
    public final Widget background;
    public final Widget checkmark;

    public RawCheckbox(boolean checked, Consumer<Boolean> onUpdate, Widget background, Widget checkmark) {
        this.checked = checked;
        this.onUpdate = onUpdate;
        this.background = background;
        this.checkmark = checkmark;
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawButton(
            () -> this.onUpdate.accept(!this.checked),
            this.checked
                ? new Stack(this.background, this.checkmark)
                : this.background
        );
    }
}
