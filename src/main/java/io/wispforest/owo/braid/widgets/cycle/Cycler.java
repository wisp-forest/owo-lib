package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class Cycler<T> extends StatelessWidget {
    //Psyckler

    public final List<T> values;
    public final int index;

    public final CyclerCallback<T> onChanged;

    public final CyclingWidgetBuilder<T> builder;

    public Cycler(List<T> values, int index, CyclerCallback<T> onChanged, CyclingWidgetBuilder<T> builder) {
        this.values = values;
        this.index = index;
        this.onChanged = onChanged;
        this.builder = builder;
    }

    @Override
    public Widget build(BuildContext context) {
        return this.builder.build(
            this.values.get(this.index),
            this.index,
            amount -> this.onChanged.cycle(this.values.get(MathHelper.floorMod(this.index + amount, this.values.size())))
        );
    }

    @FunctionalInterface
    public interface CyclerCallback<T> {
        void cycle(T newValue);
    }

    @FunctionalInterface
    public interface CyclingWidgetBuilder<T> {
        Widget build(T currentValue, int currentIndex, CycleFunction cycle);
    }

    @FunctionalInterface
    public interface CycleFunction {
        void cycle(int amount);

        default boolean forMouseButton(int button) {
            if (button != 0 && button != 1) return false;
            this.cycle(button == 0 ? 1 : -1);
            return true;
        }

        default boolean forScroll(double amount) {
            if (amount == 0) return false;
            this.cycle(amount > 0 ? 1 : -1);
            return true;
        }
    }
}
