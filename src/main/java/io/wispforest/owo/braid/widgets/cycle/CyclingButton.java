package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import io.wispforest.owo.braid.widgets.button.ButtonPanel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// A Vanilla-styled [RawCyclingButton]
///
/// @see RawCyclingButton
public class CyclingButton<T> extends StatelessWidget {

    public final List<T> values;
    public final int index;
    public final boolean wrap;
    /// The [Cycler.CyclerCallback] to invoke when this widget is cycled.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable Cycler.CyclerCallback<T> onChanged;
    public final Widget child;

    public CyclingButton(
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

    /// Create a [CyclingButton] with an explicit active state
    public CyclingButton(List<T> values, int index, boolean wrap, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        this(values, index, wrap, active ? onChanged : null, child);
    }

    /// Create a wrapping [CyclingButton]
    public CyclingButton(List<T> values, int index, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        this(values, index, true, onChanged, child);
    }

    /// Create a wrapping [CyclingButton] with an explicit active state
    public CyclingButton(List<T> values, int index, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        this(values, index, true, active ? onChanged : null, child);
    }

    /// Create a [CyclingButton] for a `boolean`
    public static CyclingButton<Boolean> forBoolean(boolean value, @Nullable Cycler.CyclerCallback<Boolean> onChanged, Widget child) {
        return new CyclingButton<>(List.of(false, true), value ? 1 : 0, true, onChanged, child);
    }

    /// Create a [CyclingButton] for a `boolean` with an explicit active state
    public static CyclingButton<Boolean> forBoolean(boolean value, Cycler.CyclerCallback<Boolean> onChanged, boolean active, Widget child) {
        return CyclingButton.forBoolean(value, active ? onChanged : null, child);
    }

    /// Create a [CyclingButton] for an [Enum]
    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, boolean wrap, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        return new CyclingButton<>(List.of(value.getDeclaringClass().getEnumConstants()), value.ordinal(), wrap, onChanged, child);
    }

    /// Create a [CyclingButton] for an [Enum] with an explicit active state
    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, boolean wrap, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        return CyclingButton.forEnum(value, wrap, active ? onChanged : null, child);
    }

    /// Create a wrapping [CyclingButton] for an [Enum]
    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        return CyclingButton.forEnum(value, true, onChanged, child);
    }

    /// Create a wrapping [CyclingButton] for an [Enum] with an explicit active state
    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        return CyclingButton.forEnum(value, true, onChanged, active, child);
    }

    @Override
    public Widget build(BuildContext context) {
        var active = this.onChanged != null && !ControlsOverride.controlsDisabled(context);
        var content = new ButtonPanel(active, this.child);
        if (!active) return content;
        return new RawCyclingButton<>(
            this.values,
            this.index,
            this.wrap,
            this.onChanged,
            content
        );
    }
}
