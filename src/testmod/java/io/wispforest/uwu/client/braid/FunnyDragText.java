package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.network.chat.Component;

public class FunnyDragText extends StatefulWidget {

    public final Component text;

    public FunnyDragText(Component text) {
        this.text = text;
    }

    @Override
    public WidgetState<FunnyDragText> createState() {
        return new State();
    }

    public static class State extends WidgetState<FunnyDragText> {

        private double x = 0, y = 0;

        @Override
        public Widget build(BuildContext context) {
            return new DragArenaElement(
                this.x,
                this.y,
                new MouseArea(
                    widget -> widget
                        .dragCallback(($, $$, dx, dy) -> this.setState(() -> {
                            this.x += dx;
                            this.y += dy;
                        }))
                        .cursorStyle(CursorStyle.HAND),
                    new Panel(
                        Panel.VANILLA_DARK,
                        new Padding(
                            Insets.all(5),
                            new Label(this.widget().text)
                        )
                    )
                )
            );
        }
    }
}
