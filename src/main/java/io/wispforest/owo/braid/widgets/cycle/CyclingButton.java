package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.button.ButtonPanel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CyclingButton<T> extends StatelessWidget {

    public final List<T> values;
    public final int index;

    public final boolean wrap;
    public final @Nullable Cycler.CyclerCallback<T> onChanged;
    public final Widget child;

    public CyclingButton(List<T> values, int index, boolean wrap, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        this.values = values;
        this.index = index;
        this.wrap = wrap;
        this.onChanged = onChanged;
        this.child = child;
    }

    public CyclingButton(List<T> values, int index, boolean wrap, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        this(values, index, wrap, active ? onChanged : null, child);
    }

    public CyclingButton(List<T> values, int index, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        this(values, index, true, onChanged, child);
    }

    public CyclingButton(List<T> values, int index, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        this(values, index, true, active ? onChanged : null, child);
    }

    public static CyclingButton<Boolean> forBoolean(boolean value, @Nullable Cycler.CyclerCallback<Boolean> onChanged, Widget child) {
        return new CyclingButton<>(List.of(false, true), value ? 1 : 0, true, onChanged, child);
    }

    public static CyclingButton<Boolean> forBoolean(boolean value, Cycler.CyclerCallback<Boolean> onChanged, boolean active, Widget child) {
        return CyclingButton.forBoolean(value, active ? onChanged : null, child);
    }

    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, boolean wrap, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        return new CyclingButton<>(List.of(value.getDeclaringClass().getEnumConstants()), value.ordinal(), wrap, onChanged, child);
    }

    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, boolean wrap, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        return CyclingButton.forEnum(value, wrap, active ? onChanged : null, child);
    }

    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, @Nullable Cycler.CyclerCallback<T> onChanged, Widget child) {
        return CyclingButton.forEnum(value, true, onChanged, child);
    }

    public static <T extends Enum<T>> CyclingButton<T> forEnum(T value, Cycler.CyclerCallback<T> onChanged, boolean active, Widget child) {
        return CyclingButton.forEnum(value, true, onChanged, active, child);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawCyclingButton<>(
            this.values,
            this.index,
            this.wrap,
            this.onChanged,
            new ButtonPanel(this.onChanged != null, this.child)
        );
    }

}
