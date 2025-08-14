package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.ui.util.UISounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// A [Widget] that implements the functionality of a cycling button
///
/// This includes clicking, scrolling, and keyboard cycling
///
/// @author chyzman
/// @see Cycler
public class RawCyclingButton<T> extends StatelessWidget {

    /// The list of values to cycle through
    public final List<T> values;
    /// The current index in the list of values
    public final int index;

    /// Whether the [Cycler] should wrap around when reaching the end of the list
    public final boolean wrap;
    /// The [Cycler.CyclerCallback] that is invoked when this [Cycler] changes
    public final @Nullable Cycler.CyclerCallback<T> onChanged;
    /// This [Widget]'s child
    public final Widget child;

    /// Create a new [RawCyclingButton]
    ///
    /// If [#onChanged] is `null`, this [RawCyclingButton] will be disabled
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

    /// Create a new [RawCyclingButton] with an explicit active state
    public RawCyclingButton(
        List<T> values,
        int index,
        boolean wrap,
        Cycler.CyclerCallback<T> onChanged,
        boolean active,
        Widget child
    ) {
        this(
            values,
            index,
            wrap,
            active ? onChanged : null,
            child
        );
    }

    /// Create a new [RawCyclingButton] that wraps
    public RawCyclingButton(
        List<T> values,
        int index,
        @Nullable Cycler.CyclerCallback<T> onChanged,
        Widget child
    ) {
        this(
            values,
            index,
            true,
            onChanged,
            child
        );
    }

    /// Create a new [RawCyclingButton] with an explicit active state that wraps
    public RawCyclingButton(
        List<T> values,
        int index,
        Cycler.CyclerCallback<T> onChanged,
        boolean active,
        Widget child
    ) {
        this(
            values,
            index,
            true,
            active ? onChanged : null,
            child
        );
    }

    /// Create a new [RawCyclingButton] for a `boolean`
    public static RawCyclingButton<Boolean> forBoolean(
        boolean value,
        @Nullable Cycler.CyclerCallback<Boolean> onChanged,
        Widget child
    ) {
        return new RawCyclingButton<>(
            List.of(false, true),
            value ? 1 : 0,
            true,
            onChanged,
            child
        );
    }

    /// Create a new [RawCyclingButton] for a `boolean` with an explicit active state
    public static RawCyclingButton<Boolean> forBoolean(
        boolean value,
        Cycler.CyclerCallback<Boolean> onChanged,
        boolean active,
        Widget child
    ) {
        return RawCyclingButton.forBoolean(
            value,
            active ? onChanged : null,
            child
        );
    }

    /// Create a new [RawCyclingButton] for an [Enum]
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(
        T value,
        boolean wrap,
        @Nullable Cycler.CyclerCallback<T> onChanged,
        Widget child
    ) {
        return new RawCyclingButton<>(
            List.of(value.getDeclaringClass().getEnumConstants()),
            value.ordinal(),
            wrap,
            onChanged,
            child
        );
    }

    /// Create a new [RawCyclingButton] for an [Enum] with an explicit active state
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(
        T value,
        boolean wrap,
        Cycler.CyclerCallback<T> onChanged,
        boolean active,
        Widget child
    ) {
        return RawCyclingButton.forEnum(
            value,
            wrap,
            active ? onChanged : null,
            child
        );
    }

    /// Create a new [RawCyclingButton] for an [Enum] that wraps
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(
        T value,
        @Nullable Cycler.CyclerCallback<T> onChanged,
        Widget child
    ) {
        return RawCyclingButton.forEnum(
            value,
            true,
            onChanged,
            child
        );
    }

    /// Create a new [RawCyclingButton] for an [Enum] with an explicit active state that wraps
    public static <T extends Enum<T>> RawCyclingButton<T> forEnum(
        T value,
        Cycler.CyclerCallback<T> onChanged,
        boolean active,
        Widget child
    ) {
        return RawCyclingButton.forEnum(
            value,
            true,
            onChanged,
            active,
            child
        );
    }

    @Override
    public Widget build(BuildContext context) {
        if (this.onChanged == null) return this.child;
        return new Cycler<>(
            this.values,
            this.index,
            this.wrap,
            this.onChanged,
            (currentValue, currentIndex, cycle) ->
                new MouseArea(
                    widget ->
                        widget.scrollCallback((horizontal, vertical) -> cycle.forScroll(vertical))
                            .cursorStyle(CursorStyle.HAND),
                    new Actions(
                        widget -> widget
                            .addAction(ActionTrigger.INCREMENT, () -> {if (cycle.cycle(1)) UISounds.playButtonSound();})
                            .addAction(ActionTrigger.DECREMENT, () -> {if (cycle.cycle(-1)) UISounds.playButtonSound();}),
                        child
                    )
                )
        );
    }
}
