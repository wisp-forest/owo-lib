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

    public final MessageProvider<T> messageProvider;

    public final Cycler<T> cycler;

    public final @Nullable Consumer<T> onChanged;

    public RawCyclingButton(
        T value,
        MessageProvider<T> messageProvider,
        Cycler<T> cycler,
        @Nullable Consumer<T> onChanged
    ) {
        this.value = value;
        this.messageProvider = messageProvider;
        this.cycler = cycler;
        this.onChanged = onChanged;
    }

    public RawCyclingButton(
        T value,
        MessageProvider<T> messageProvider,
        Cycler<T> cycler,
        Consumer<T> onChanged,
        boolean enabled
    ) {
        this(value, messageProvider, cycler, enabled ? onChanged : null);
    }


    @Override
    public Widget build(BuildContext context) {
        return new MouseArea(
            widget -> widget.scrollCallback((horizontal, vertical) -> {
                if (this.onChanged == null) return;
                this.onChanged.accept(this.cycler.cycle((int) -vertical));
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
    public interface MessageProvider<T> {
        Text getMessage(T value);
    }

    @FunctionalInterface
    public interface Cycler<T> {
        T cycle(int amount);
    }
}
