package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
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
        return new State();
    }

    public static class State extends WidgetState<Button> {

        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .clickCallback((x, y) -> {
                        this.widget().onClick.run();
                        UISounds.playButtonSound();
                    })
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false))
                    .cursorStyle(CursorStyle.HAND),
                new Panel(
                    this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE,
                    new Padding(
                        Insets.all(5),
                        new Label(
                            new LabelStyle(null, null, null, true),
                            true,
                            this.widget().text
                        )
                    )
                )
            );
        }
    }
}
