package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnumCyclingButton<T extends Enum<T>> extends StatelessWidget {

    public final T value;

    public RawCyclingButton.MessageProvider<T> messageProvider;

    public @Nullable Consumer<T> onChanged;

    public EnumCyclingButton(
        T value,
        RawCyclingButton.MessageProvider<T> messageProvider,
        @Nullable Consumer<T> onChanged
    ) {
        this.value = value;
        this.messageProvider = messageProvider;
        this.onChanged = onChanged;
    }

    public EnumCyclingButton(
        T value,
        RawCyclingButton.MessageProvider<T> messageProvider,
        Consumer<T> onChanged,
        boolean enabled
    ) {
        this(value, messageProvider, enabled ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawCyclingButton<>(
            this.value,
            this.messageProvider,
            amount -> {
                var values = this.value.getDeclaringClass().getEnumConstants();
                var index = (this.value.ordinal() + amount + values.length) % values.length;
                return values[index];
            },
            this.onChanged
        );
    }
}
