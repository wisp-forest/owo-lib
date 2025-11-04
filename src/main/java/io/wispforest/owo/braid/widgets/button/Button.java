package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import io.wispforest.owo.braid.widgets.basic.Padding;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class Button extends StatelessWidget {

    public final @Nullable BooleanSupplier onClick;
    public final @Nullable ButtonStyle style;
    public final Widget child;

    public Button(@Nullable BooleanSupplier onClick, @Nullable ButtonStyle style, Widget child) {
        this.onClick = onClick;
        this.style = style;
        this.child = child;
    }

    public Button(@Nullable Runnable onClick, @Nullable ButtonStyle style, Widget child) {
        this(Clickable.alwaysClick(onClick), style, child);
    }

    public Button(@Nullable BooleanSupplier onClick, Widget child) {
        this(onClick, null, child);
    }

    public Button(@Nullable Runnable onClick, Widget child) {
        this(Clickable.alwaysClick(onClick), child);
    }

    public Button(boolean active, BooleanSupplier onClick, @Nullable ButtonStyle style, Widget child) {
        this(active ? onClick : null, style, child);
    }

    public Button(boolean active, Runnable onClick, @Nullable ButtonStyle style, Widget child) {
        this(active, Clickable.alwaysClick(onClick), style, child);
    }

    public Button(boolean active, BooleanSupplier onClick, Widget child) {
        this(active, onClick, null, child);
    }

    public Button(boolean active, Runnable onClick, Widget child) {
        this(active, Clickable.alwaysClick(onClick), child);
    }

    @Override
    public Widget build(BuildContext context) {
        var effectiveStyle = this.style != null ? this.style : ButtonStyle.EMPTY;
        if (DefaultButtonStyle.maybeOf(context) instanceof ButtonStyle contextStyle) {
            effectiveStyle = effectiveStyle.overriding(contextStyle);
        }

        Widget content = new Padding(
            effectiveStyle.padding() != null
                ? effectiveStyle.padding()
                : Insets.all(5),
            this.child
        );

        var disabled = this.onClick == null || ControlsOverride.controlsDisabled(context);
        content = effectiveStyle.builder() != null
            ? effectiveStyle.builder().build(!disabled, content)
            : new ButtonPanel(!disabled, content);

        return !disabled
            ? new Clickable(this.onClick, effectiveStyle.clickSound(), content)
            : content;
    }
}

