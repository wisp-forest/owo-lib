package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.List;

public class Cycler<T> extends StatelessWidget {
    //Psyckler

    public final List<T> values;
    public final int currentIndex;

    public final boolean wrap;
    public final CyclerCallback<T> onChanged;

    public final CyclingWidgetBuilder<T> builder;

    public Cycler(List<T> values, T currentValue, boolean wrap, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        this.values = values;
        this.currentIndex = this.values.indexOf(currentValue);
        this.wrap = wrap;
        this.onChanged = onChanged;
        this.builder = builder;
    }

    public Cycler(List<T> values, T currentValue, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        this(values, currentValue, true, onChanged, builder);
    }

    public static Cycler<Boolean> forBoolean(boolean value, boolean wrap, CyclerCallback<Boolean> onChanged, CyclingWidgetBuilder<Boolean> builder) {
        return new Cycler<>(List.of(false, true), value, wrap, onChanged, builder);
    }

    public static Cycler<Boolean> forBoolean(boolean value, CyclerCallback<Boolean> onChanged, CyclingWidgetBuilder<Boolean> builder) {
        return Cycler.forBoolean(value, true, onChanged, builder);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Cycler<T> forEnum(T value, boolean wrap, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        return new Cycler<>((List<T>) Arrays.stream(value.getClass().getEnumConstants()).toList(), value, wrap, onChanged, builder);
    }

    public static <T extends Enum<T>> Cycler<T> forEnum(T value, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        return Cycler.forEnum(value, true, onChanged, builder);
    }

    @Override
    public Widget build(BuildContext context) {
        return this.builder.build(
            this.values.get(this.currentIndex),
            this.currentIndex,
            amount -> {
                var newIndex = this.wrap ? Mth.positiveModulo(this.currentIndex + amount, this.values.size()) : Mth.clamp(this.currentIndex + amount, 0, this.values.size() - 1);
                if (newIndex == this.currentIndex) return false;
                this.onChanged.cycle(this.values.get(newIndex), newIndex);
                return true;
            }
        );
    }

    @FunctionalInterface
    public interface CyclerCallback<T> {
        void cycle(T newValue, int newIndex);
    }

    @FunctionalInterface
    public interface CyclingWidgetBuilder<T> {
        Widget build(T currentValue, int currentIndex, CycleFunction cycle);
    }

    @FunctionalInterface
    public interface CycleFunction {
        boolean cycle(int amount);

        default boolean forScroll(double amount) {
            if (amount == 0) return false;
            return this.cycle(amount > 0 ? 1 : -1);
        }
    }
}
