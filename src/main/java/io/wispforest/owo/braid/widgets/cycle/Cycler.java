package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;

/// A [Widget] that allows cycling through a list of values
///
/// @author chyzman
public class Cycler<T> extends StatelessWidget {
    //Psyckler

    /// The list of values to cycle through
    public final List<T> values;
    /// The current index in the list of values
    public final int index;

    /// Whether the [Cycler] should wrap around when reaching the end of the list
    public final boolean wrap;
    /// The [CyclerCallback] that is invoked when this [Cycler] changes
    public final CyclerCallback<T> onChanged;

    /// The [CyclingWidgetBuilder] that is used to build this [Cycler]'s child [Widget]
    public final CyclingWidgetBuilder<T> builder;


    /// Create a new [Cycler]
    public Cycler(
        List<T> values,
        int index,
        boolean wrap,
        CyclerCallback<T> onChanged,
        CyclingWidgetBuilder<T> builder
    ) {
        this.values = values;
        this.index = index;
        this.wrap = wrap;
        this.onChanged = onChanged;
        this.builder = builder;
    }

    /// Create a new [Cycler] that wraps
    public Cycler(
        List<T> values,
        int index,
        CyclerCallback<T> onChanged,
        CyclingWidgetBuilder<T> builder
    ) {
        this(
            values,
            index,
            true,
            onChanged,
            builder
        );
    }

    /// Create a new [Cycler] for a `boolean`
    public static Cycler<Boolean> forBoolean(
        boolean value,
        boolean wrap,
        CyclerCallback<Boolean> onChanged,
        CyclingWidgetBuilder<Boolean> builder
    ) {
        return new Cycler<>(
            List.of(false, true),
            value ? 1 : 0,
            wrap,
            onChanged,
            builder
        );
    }

    /// Create a new [Cycler] for a `boolean` that wraps
    public static Cycler<Boolean> forBoolean(
        boolean value,
        CyclerCallback<Boolean> onChanged,
        CyclingWidgetBuilder<Boolean> builder
    ) {
        return Cycler.forBoolean(
            value,
            true,
            onChanged,
            builder
        );
    }

    /// Creates a new [Cycler] for an [Enum]
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Cycler<T> forEnum(
        T value,
        boolean wrap,
        CyclerCallback<T> onChanged,
        CyclingWidgetBuilder<T> builder
    ) {
        return new Cycler<>(
            (List<T>) Arrays.stream(value.getClass().getEnumConstants()).toList(),
            value.ordinal(),
            wrap,
            onChanged,
            builder
        );
    }

    /// Creates a new [Cycler] for an [Enum] that wraps
    public static <T extends Enum<T>> Cycler<T> forEnum(
        T value,
        CyclerCallback<T> onChanged,
        CyclingWidgetBuilder<T> builder
    ) {
        return Cycler.forEnum(
            value,
            true,
            onChanged,
            builder
        );
    }

    @Override
    public Widget build(BuildContext context) {
        return this.builder.build(
            this.values.get(this.index),
            this.index,
            amount -> {
                var newIndex = this.wrap ? MathHelper.floorMod(this.index + amount, this.values.size()) : MathHelper.clamp(this.index + amount, 0, this.values.size() - 1);
                if (newIndex == this.index) return false;
                this.onChanged.cycle(this.values.get(newIndex), newIndex);
                return true;
            }
        );
    }

    /// A Callback for when a [Cycler] changes its value
    @FunctionalInterface
    public interface CyclerCallback<T> {
        void cycle(T newValue, int newIndex);
    }

    /// An interface for building the child [Widget] of a [Cycler]
    @FunctionalInterface
    public interface CyclingWidgetBuilder<T> {
        Widget build(T currentValue, int currentIndex, CycleFunction cycle);
    }

    /// The interface used to cycle through values in a [Cycler]
    @FunctionalInterface
    public interface CycleFunction {
        boolean cycle(int amount);

        default boolean forScroll(double amount) {
            if (amount == 0) return false;
            return this.cycle(amount > 0 ? 1 : -1);
        }
    }
}
