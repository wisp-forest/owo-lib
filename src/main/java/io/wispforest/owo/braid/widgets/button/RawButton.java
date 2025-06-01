package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.ui.util.UISounds;
import org.jetbrains.annotations.Nullable;
import java.util.function.IntPredicate;

public class RawButton extends StatelessWidget {

    public final @Nullable IntPredicate onClick;
    public final Widget child;

    public RawButton(@Nullable IntPredicate onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    public RawButton(IntPredicate onClick, boolean active, Widget child) {
        this(active ? onClick : null, child);
    }

    @Override
    public Widget build(BuildContext context) {
        if (this.onClick == null) return this.child;
        return new MouseArea(
            widget -> widget
                .clickCallback((x, y, button) -> {
                    var result = this.onClick.test(button);
                    UISounds.playButtonSound();
                    //TODO: once actions exist, return result here
                })
                .cursorStyle(CursorStyle.HAND),
            child
        );
    }
}
