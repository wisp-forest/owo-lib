package io.wispforest.owo.braid.widgets.slider.xlyder;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class MessageXlyder extends StatelessWidget {

    public final Vector2dc value;
    public final @Nullable WidgetSetupCallback<Xlyder> setupCallback;
    public final @Nullable XlyderCallback onChanged;

    public final Component message;

    public MessageXlyder(
        Vector2dc value,
        Component message,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageXlyder(
        Vector2dc value,
        Component message,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged
    ) {
        this(value, message, setupCallback, active ? onChanged : null);
    }

    public MessageXlyder(
        double x, double y,
        Component message,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged
    ) {
        this(new Vector2d(x, y), message, setupCallback, onChanged);
    }

    public MessageXlyder(
        double x, double y,
        Component message,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged
    ) {
        this(new Vector2d(x, y), message, setupCallback, active ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Xlyder(
                this.value,
                this.setupCallback, this.onChanged
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
        Component getMessage(double x, double y);
    }
}
