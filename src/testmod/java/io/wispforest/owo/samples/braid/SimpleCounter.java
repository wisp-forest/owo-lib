package io.wispforest.owo.samples.braid;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import net.minecraft.network.chat.Component;

public class SimpleCounter extends StatefulWidget {
    @Override
    public WidgetState<SimpleCounter> createState() {
        return new State();
    }

    public static class State extends WidgetState<SimpleCounter> {

        private int count = 0;

        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Panel(
                    Panel.VANILLA_LIGHT,
                    new Padding(
                        Insets.all(10),
                        new MessageButton(
                            Component.literal("Count: " + this.count),
                            () -> this.setState(() -> {
                                this.count++;
                            })
                        )
                    )
                )
            );
        }
    }
}
