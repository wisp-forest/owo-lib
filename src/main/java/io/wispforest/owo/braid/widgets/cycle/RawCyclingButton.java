package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.button.RawButton;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RawCyclingButton<T> extends StatelessWidget {

    public final T value;

    public final Cycler<T> cycler;

    public final CyclerMessageProvider<T> messageProvider;

    public final @Nullable Consumer<T> onChanged;

    public RawCyclingButton(
        T value,
        Cycler<T> cycler,
        CyclerMessageProvider<T> messageProvider,
        @Nullable Consumer<T> onChanged
    ) {
        this.value = value;
        this.cycler = cycler;
        this.messageProvider = messageProvider;
        this.onChanged = onChanged;
    }

    public RawCyclingButton(
        T value,
        Cycler<T> cycler,
        CyclerMessageProvider<T> messageProvider,
        Consumer<T> onChanged,
        boolean enabled
    ) {
        this(value, cycler, messageProvider, enabled ? onChanged : null);
    }


    @Override
    public Widget build(BuildContext context) {
        return new MouseArea(
            widget -> widget.scrollCallback((horizontal, vertical) -> {
                if (this.onChanged == null || vertical == 0) return;
                this.onChanged.accept(this.cycler.cycle(vertical < 0 ? -1 : 1));
            }),
            new RawButton(
                this.messageProvider.getMessage(this.value),
                this.onChanged == null ? null : button -> {
                    if (button != 0 && button != 1) return;
                    //TODO: check if shift is pressed
                    this.onChanged.accept(this.cycler.cycle(button == 0 ? 1 : -1));
                }
            )
        );
    }

    @FunctionalInterface
    public interface Cycler<T> {
        T cycle(int amount);
    }
}
