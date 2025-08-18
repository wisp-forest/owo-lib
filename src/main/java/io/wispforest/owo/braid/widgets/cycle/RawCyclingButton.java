package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.button.RawButton;
import io.wispforest.owo.ui.util.UISounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// A [Widget],much like [RawButton], that uses a [Cycler] to cycle through a list of values.<br>
/// When active and hovered, the cursor uses [CursorStyle#HAND].
///
/// If no [#onChanged] is provided, this widget is inactive and falls through to its child
/// @see RawButton
public class RawCyclingButton<T> extends StatelessWidget {

    public final List<T> values;
    public final int index;
    public final boolean wrap;
    /// The [Cycler.CyclerCallback] to invoke when this widget is cycled.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable Cycler.CyclerCallback<T> onChanged;
    public final Widget child;

    public RawCyclingButton(
        List<T> values,
        int index,
        boolean wrap,
        @Nullable Cycler.CyclerCallback<T> onChanged,
        Widget child
    ) {
        this.values = values;
        this.index = index;
        this.wrap = wrap;
        this.onChanged = onChanged;
        this.child = child;
    }

    /// Create a [RawCyclingButton] with an explicit active state
    public RawCyclingButton(List<T> values, int index, boolean wrap, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        this(values, index, wrap, active ? onChanged : null, child);
    }

    /// Create a wrapping [RawCyclingButton]
    public RawCyclingButton(List<T> values, int index, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        this(values, index, true, onChanged, child);
    }

    /// Create a wrapping [RawCyclingButton] with an explicit active state
    public RawCyclingButton(List<T> values, int index, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        this(values, index, true, active ? onChanged : null, child);
    }

    /// Create a [RawCyclingButton] for a `boolean`
    public static RawCyclingButton<Boolean> forBoolean(boolean value, @Nullable Cycler.CyclerCallback<Boolean> onChanged, Widget child) {
        return new RawCyclingButton<>(List.of(false, true), value ? 1 : 0, true, onChanged, child);
    }

    /// Create a [RawCyclingButton] for a `boolean` with an explicit active state
    public static RawCyclingButton<Boolean> forBoolean(boolean value, Cycler.CyclerCallback<Boolean> onChanged, boolean active, Widget child) {
        return RawCyclingButton.forBoolean(value, active ? onChanged : null, child);
    }

    /// Create a [RawCyclingButton] for an [Enum]
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(T value, boolean wrap, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        return new RawCyclingButton<>(List.of(value.getDeclaringClass().getEnumConstants()), value.ordinal(), wrap, onChanged, child);
    }

    /// Create a [RawCyclingButton] for an [Enum] with an explicit active state
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(T value, boolean wrap, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        return RawCyclingButton.forEnum(value, wrap, active ? onChanged : null, child);
    }

    /// Create a wrapping [RawCyclingButton] for an [Enum]
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(T value, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        return RawCyclingButton.forEnum(value, true, onChanged, child);
    }

    /// Create a wrapping [RawCyclingButton] for an [Enum] with an explicit active state
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(T value, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        return RawCyclingButton.forEnum(value, true, onChanged, active, child);
    }

    @Override
    public Widget build(BuildContext context) {
        if (this.onChanged == null || ControlsOverride.controlsDisabled(context)) return this.child;
        return new Cycler<>(
            this.values,
            this.index,
            this.wrap,
            this.onChanged,
            (currentValue, currentIndex, cycler) ->
                new MouseArea(
                    widget ->
                        widget.scrollCallback((horizontal, vertical) -> cycler.forScroll(vertical))
                            .cursorStyle(CursorStyle.HAND),
                    new Actions(
                        widget -> widget
                            .addAction(ActionTrigger.INCREMENT, () -> {if (cycler.cycle(1)) UISounds.playButtonSound();})
                            .addAction(ActionTrigger.DECREMENT, () -> {if (cycler.cycle(-1)) UISounds.playButtonSound();}),
                        child
                    )
                )
        );
    }
}
