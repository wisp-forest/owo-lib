package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.button.RawButton;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CyclingButton<T> extends StatelessWidget {

    public final T value;
    public final int index;

    public final List<T> values;

    public final CyclerMessageProvider<T> messageProvider;

    public final @Nullable Consumer<T> onChanged;

    public CyclingButton(
        T value,
        int index,
        List<T> values,
        CyclerMessageProvider<T> messageProvider,
        @Nullable Consumer<T> onChanged
    ) {
        this.value = value;
        this.index = index;
        this.values = values;
        this.messageProvider = messageProvider;
        this.onChanged = onChanged;
    }

    public CyclingButton(
        T value,
        List<T> values,
        CyclerMessageProvider<T> messageProvider,
        @Nullable Consumer<T> onChanged
    ) {
        this(value, values.indexOf(value), values, messageProvider, onChanged);
    }

    public CyclingButton(
        T value,
        int index,
        List<T> values,
        CyclerMessageProvider<T> messageProvider,
        Consumer<T> onChanged,
        boolean enabled
    ) {
        this(value, index, values, messageProvider, enabled ? onChanged : null);
    }

    public CyclingButton(
        T value,
        List<T> values,
        CyclerMessageProvider<T> messageProvider,
        Consumer<T> onChanged,
        boolean enabled
    ) {
        this(value, values, messageProvider, enabled ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawCyclingButton<>(
            this.value,
            amount -> this.values.get(MathHelper.floorMod(this.index + amount, this.values.size())),
            this.messageProvider,
            this.onChanged
        );
    }
}
