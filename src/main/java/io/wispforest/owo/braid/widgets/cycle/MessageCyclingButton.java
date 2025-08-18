package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// A Vanilla-styled [RawCyclingButton] that displays a message.<br>
/// This is the braid equivalent of [net.minecraft.client.gui.widget.CyclingButtonWidget]
///
/// @see CyclingButton
/// @see Label
public class MessageCyclingButton<T> extends StatelessWidget {

    public final List<T> values;
    public final int index;
    public final boolean wrap;
    public final Text text;
    /// The [Cycler.CyclerCallback] to invoke when this widget is cycled.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable Cycler.CyclerCallback<T> onChanged;

    public MessageCyclingButton(
        List<T> values,
        int index,
        boolean wrap,
        Text text,
        @Nullable Cycler.CyclerCallback<T> onChanged
    ) {
        this.values = values;
        this.index = index;
        this.wrap = wrap;
        this.text = text;
        this.onChanged = onChanged;
    }

    /// Create a `MessageCyclingButton` with an explicit active state
    public MessageCyclingButton(List<T> values, int index, boolean wrap, Text text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        this(values, index, wrap, text, active ? onChanged : null);
    }

    /// Create a wrapping `MessageCyclingButton`
    public MessageCyclingButton(List<T> values, int index, Text text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        this(values, index, true, text, onChanged);
    }

    /// Create a wrapping `MessageCyclingButton` with an explicit active state
    public MessageCyclingButton(List<T> values, int index, Text text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        this(values, index, true, text, onChanged, active);
    }

    /// Create a `MessageCyclingButton` for a `boolean`
    public static MessageCyclingButton<Boolean> forBoolean(boolean value, Text text, @Nullable Cycler.CyclerCallback<Boolean> onChanged) {
        return new MessageCyclingButton<>(List.of(false, true), value ? 1 : 0, true, text, onChanged);
    }

    /// Create a `MessageCyclingButton` for a `boolean` with an explicit active state
    public static MessageCyclingButton<Boolean> forBoolean(boolean value, Text text, Cycler.CyclerCallback<Boolean> onChanged, boolean active) {
        return MessageCyclingButton.forBoolean(value, text, active ? onChanged : null);
    }

    /// Create a `MessageCyclingButton` for an [Enum]
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, boolean wrap, Text text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        return new MessageCyclingButton<>(List.of(value.getDeclaringClass().getEnumConstants()), value.ordinal(), wrap, text, onChanged);
    }

    /// Create a `MessageCyclingButton` for an [Enum] with an explicit active state
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, boolean wrap, Text text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        return MessageCyclingButton.forEnum(value, wrap, text, active ? onChanged : null);
    }

    /// Create a wrapping `MessageCyclingButton` for an [Enum]
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, Text text, @Nullable Cycler.CyclerCallback<T> onChanged) {
        return MessageCyclingButton.forEnum(value, true, text, onChanged);
    }

    /// Create a wrapping `MessageCyclingButton` for an [Enum] with an explicit active state
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(T value, Text text, Cycler.CyclerCallback<T> onChanged, boolean active) {
        return MessageCyclingButton.forEnum(value, true, text, active ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new CyclingButton<>(
            this.values,
            this.index,
            this.wrap,
            this.onChanged,
            new Label(
                this.onChanged != null
                    ? LabelStyle.SHADOW
                    : new LabelStyle(null, Color.ofFormatting(Formatting.GRAY), null, false),
                true,
                this.text
            )
        );
    }

}
