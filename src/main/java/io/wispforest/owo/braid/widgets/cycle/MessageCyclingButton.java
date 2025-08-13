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

/// A [CyclingButton] with a [Label] as its child
///
/// @author chyzman
/// @see CyclingButton
/// @see Label
public class MessageCyclingButton<T> extends StatelessWidget {

    /// The list of values to cycle through
    public final List<T> values;
    /// The current index in the list of values
    public final int index;

    /// Whether this [MessageCyclingButton] should wrap around when reaching the end of the list
    public final boolean wrap;
    /// The [Text] that is displayed as the child of this [MessageCyclingButton]
    public final Text text;
    /// The [Cycler.CyclerCallback] that is invoked when this [MessageCyclingButton] changes
    public final @Nullable Cycler.CyclerCallback<T> onChanged;

    /// Create a new [MessageCyclingButton]
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

    /// Create a new [MessageCyclingButton] with an explicit active state
    public MessageCyclingButton(
        List<T> values,
        int index,
        boolean wrap,
        Text text,
        Cycler.CyclerCallback<T> onChanged,
        boolean active
    ) {
        this(
            values,
            index,
            wrap,
            text,
            active ? onChanged : null
        );
    }

    /// Create a new [MessageCyclingButton] that wraps
    public MessageCyclingButton(
        List<T> values,
        int index,
        Text text,
        @Nullable Cycler.CyclerCallback<T> onChanged
    ) {
        this(
            values,
            index,
            true,
            text,
            onChanged
        );
    }

    /// Create a new [MessageCyclingButton] that wraps with an explicit active state
    public MessageCyclingButton(
        List<T> values,
        int index,
        Text text,
        Cycler.CyclerCallback<T> onChanged,
        boolean active
    ) {
        this(
            values,
            index,
            true,
            text,
            onChanged,
            active
        );
    }

    /// Create a new [MessageCyclingButton] for a `boolean`
    public static MessageCyclingButton<Boolean> forBoolean(
        boolean value,
        Text text,
        @Nullable Cycler.CyclerCallback<Boolean> onChanged
    ) {
        return new MessageCyclingButton<>(
            List.of(false, true),
            value ? 1 : 0,
            true,
            text,
            onChanged
        );
    }

    /// Create a new [MessageCyclingButton] for a `boolean` that wraps
    public static MessageCyclingButton<Boolean> forBoolean(
        boolean value,
        Text text,
        Cycler.CyclerCallback<Boolean> onChanged,
        boolean active
    ) {
        return MessageCyclingButton.forBoolean(
            value,
            text,
            active ? onChanged : null
        );
    }

    /// Create a new [MessageCyclingButton] for an [Enum]
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(
        T value,
        boolean wrap,
        Text text,
        @Nullable Cycler.CyclerCallback<T> onChanged
    ) {
        return new MessageCyclingButton<>(
            List.of(value.getDeclaringClass().getEnumConstants()),
            value.ordinal(),
            wrap,
            text,
            onChanged
        );
    }

    /// Create a new [MessageCyclingButton] for an [Enum] with an explicit active state
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(
        T value,
        boolean wrap,
        Text text,
        Cycler.CyclerCallback<T> onChanged,
        boolean active
    ) {
        return MessageCyclingButton.forEnum(
            value,
            wrap,
            text,
            active ? onChanged : null
        );
    }

    /// Create a new [MessageCyclingButton] for an [Enum] that wraps
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(
        T value,
        Text text,
        @Nullable Cycler.CyclerCallback<T> onChanged
    ) {
        return MessageCyclingButton.forEnum(
            value,
            true,
            text,
            onChanged
        );
    }

    /// Create a new [MessageCyclingButton] for an [Enum] with an explicit active state that wraps
    public static <T extends Enum<T>> MessageCyclingButton<T> forEnum(
        T value,
        Text text,
        Cycler.CyclerCallback<T> onChanged,
        boolean active
    ) {
        return MessageCyclingButton.forEnum(
            value,
            true,
            text,
            active ? onChanged : null
        );
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
