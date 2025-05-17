package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BooleanCyclingButton extends StatelessWidget {

    public final boolean value;

    public CyclerMessageProvider<Boolean> messageProvider;

    public @Nullable Consumer<Boolean> onChanged;

    public BooleanCyclingButton(
        boolean value,
        CyclerMessageProvider<Boolean> messageProvider,
        @Nullable Consumer<Boolean> onChanged
    ) {
        this.value = value;
        this.messageProvider = messageProvider;
        this.onChanged = onChanged;
    }

    public BooleanCyclingButton(
        boolean value,
        CyclerMessageProvider<Boolean> messageProvider,
        Consumer<Boolean> onChanged,
        boolean enabled
    ) {
        this(value, messageProvider, enabled ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new CyclingButton<>(this.value, List.of(false, true), this.messageProvider, this.onChanged);
    }
}
