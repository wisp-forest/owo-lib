package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;

public class Button extends StatefulWidget {

    public final Text text;
    public final Runnable onClick;

    public Button(Text text, Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    public WidgetState<Button> createState() {
        return new ButtonState();
    }
}

class ButtonState extends WidgetState<Button> {

    private boolean hovered = false;

    @Override
    public Widget build(BuildContext context) {
        return new MouseArea(
            (x, y) -> {
                this.widget().onClick.run();
                UISounds.playButtonSound();
                },
            () -> this.setState(() -> this.hovered = true),
            () -> this.setState(() -> this.hovered = false),
            null, null, null, null,
            (x, y) -> CursorStyle.HAND,
            new Panel(
                this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE,
                new Padding(
                    Insets.both(5, 5),
                    new Label(
                        new LabelStyle(Alignment.CENTER, Color.WHITE, true),
                        true,
                        this.widget().text
                    )
                )
            )
        );
    }
}
