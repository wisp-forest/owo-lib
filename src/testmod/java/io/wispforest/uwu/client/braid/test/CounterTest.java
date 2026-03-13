package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.network.chat.Component;

public class CounterTest extends StatefulWidget {
    @Override
    public WidgetState<CounterTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<CounterTest> {
        private int count = 0;

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                50.0,
                null,
                new Column(
                    new Label(Component.literal("count: " + this.count)),
                    new Row(
                        new Flexible(
                            new MessageButton(
                                Component.literal("+"),
                                () -> this.setState(() -> this.count++)
                            )
                        ),
                        new Flexible(
                            new MessageButton(
                                Component.literal("-"),
                                () -> this.setState(() -> this.count--)
                            )
                        )
                    )
                )
            );
        }
    }
}
