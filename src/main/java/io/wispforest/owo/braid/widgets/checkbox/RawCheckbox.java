package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.util.UISounds;
import org.lwjgl.glfw.GLFW;

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
        return new MouseArea(
            widget -> widget
                .cursorStyle(CursorStyle.HAND)
                .clickCallback((x, y, button) -> {
                    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

                    this.onUpdate.accept(!this.checked);
                    UISounds.playButtonSound();
                }),
            this.checked
                ? new Stack(this.background, this.checkmark)
                : this.background
        );
    }
}
