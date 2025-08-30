package io.wispforest.owo.braid.widgets.slider.xlyder;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.slider.slider.RawSlider;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class MessageXlyder extends StatelessWidget {

    public final Vector2dc value;
    public final @Nullable RawXlyder.XlyderSetupCallback<Xlyder> setupCallback;
    public final @Nullable XlyderCallback onChanged;

    public final Text message;

    public MessageXlyder(
        Vector2dc value,
        @Nullable RawXlyder.XlyderSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged,
        Text message
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageXlyder(
        Vector2dc value,
        @Nullable RawXlyder.XlyderSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged,
        Text message
    ) {
        this(value, setupCallback, active ? onChanged : null, message);
    }

    public MessageXlyder(
        double x, double y,
        @Nullable RawXlyder.XlyderSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged,
        Text message
    ) {
        this(new Vector2d(x, y), setupCallback, onChanged, message);
    }

    public MessageXlyder(
        double x, double y,
        @Nullable RawXlyder.XlyderSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged,
        Text message
    ) {
        this(new Vector2d(x, y), setupCallback, active ? onChanged : null, message);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Xlyder(
                this.value,
                this.setupCallback,
                this.onChanged
            ),
            //TODO: abstract this styling?
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }

    @FunctionalInterface
    public interface XlyderMessageProvider {
        Text getMessage(double x, double y);
    }
}
