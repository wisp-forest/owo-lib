package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MessageCyclingButton<T> extends StatelessWidget {

    public final List<T> values;
    public final T currentValue;

    public final boolean wrap;
    public final Component text;
    public final @Nullable Cycler.CyclerCallback<T> onChanged;

    public MessageCyclingButton(List<T> values, T currentValue, boolean wrap, Component text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        this.values = values;
        this.currentValue = currentValue;
        this.wrap = wrap;
        this.text = text;
        this.onChanged = onChanged;
    }

    public MessageCyclingButton(List<T> values, T currentValue, boolean wrap, Component text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        this(values, currentValue, wrap, text, active ? onChanged : null);
    }

    public MessageCyclingButton(List<T> values, T currentValue, Component text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        this(values, currentValue, true, text, onChanged);
    }

    public MessageCyclingButton(List<T> values, T currentValue, Component text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        this(values, currentValue, true, text, onChanged, active);
    }

    public static MessageCyclingButton<Boolean> forBoolean(boolean value, Component text, @Nullable Cycler.CyclerCallback<Boolean> onChanged) {
        return new MessageCyclingButton<>(List.of(false, true), value, true, text, onChanged);
    }

    public static MessageCyclingButton<Boolean> forBoolean(boolean value, Component text, Cycler.CyclerCallback<Boolean> onChanged, boolean active) {
        return MessageCyclingButton.forBoolean(value, text, active ? onChanged : null);
    }

    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, boolean wrap, Component text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        return new MessageCyclingButton<>(List.of(value.getDeclaringClass().getEnumConstants()), value, wrap, text, onChanged);
    }

    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, boolean wrap, Component text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        return MessageCyclingButton.forEnum(value, wrap, text, active ? onChanged : null);
    }

    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, Component text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        return MessageCyclingButton.forEnum(value, true, text, onChanged);
    }

    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, Component text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        return MessageCyclingButton.forEnum(value, true, text, active ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new CyclingButton<>(
            this.values,
            this.currentValue,
            this.wrap,
            this.onChanged,
            //TODO: abstract away the million places where a ternary operator is used to determine the label style for a possibly disabled button
            new Label(
                this.onChanged != null
                    ? LabelStyle.SHADOW
                    : new LabelStyle(null, Color.formatting(ChatFormatting.GRAY), null, false),
                true,
                this.text
            )
        );
    }

}
