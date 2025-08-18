package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;

/// A [Widget] that allows cycling through a list of values.
public class Cycler<T> extends StatelessWidget {
    //Psyckler

    public final List<T> values;
    public final int index;
    public final boolean wrap;
    /// The [CyclerCallback] to invoke when this widget is cycled.<br>
    public final CyclerCallback<T> onChanged;
    /// The [CyclingWidgetBuilder] used to build this widget's child
    public final CyclingWidgetBuilder<T> builder;

    public Cycler(List<T> values, int index, boolean wrap, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        this.values = values;
        this.index = index;
        this.wrap = wrap;
        this.onChanged = onChanged;
        this.builder = builder;
    }

    /// Create a wrapping `Cycler`
    public Cycler(List<T> values, int index, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        this(values, index, true, onChanged, builder);
    }

    /// Create a [Cycler] for a `boolean`
    public static Cycler<Boolean> forBoolean(boolean value, boolean wrap, CyclerCallback<Boolean> onChanged, CyclingWidgetBuilder<Boolean> builder) {
        return new Cycler<>(List.of(false, true), value ? 1 : 0, wrap, onChanged, builder);
    }

    /// Create a wrapping `Cycler` for a `boolean`
    public static Cycler<Boolean> forBoolean(boolean value, CyclerCallback<Boolean> onChanged, CyclingWidgetBuilder<Boolean> builder) {
        return Cycler.forBoolean(value, true, onChanged, builder);
    }

    /// Create a `Cycler` for an [Enum]
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Cycler<T> forEnum(T value, boolean wrap, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        return new Cycler<>((List<T>) Arrays.stream(value.getClass().getEnumConstants()).toList(), value.ordinal(), wrap, onChanged, builder);
    }

    /// Create a wrapping `Cycler` for an [Enum]
    public static <T extends Enum<T>> Cycler<T> forEnum(T value, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        return Cycler.forEnum(value, true, onChanged, builder);
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

    /// An interface for building a [Cycler]'s child
    @FunctionalInterface
    public interface CyclingWidgetBuilder<T> {
        Widget build(T currentValue, int currentIndex, CycleFunction cycler);
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
