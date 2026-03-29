package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Flex;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.network.chat.Component;

public class FlexTest extends StatefulWidget {
    @Override
    public WidgetState<FlexTest> createState() {
        return new State();
    }

    private static class State extends WidgetState<FlexTest> {
        private LayoutAxis axis = LayoutAxis.HORIZONTAL;

        @Override
        public Widget build(BuildContext context) {
            return new Flex(
                this.axis,
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new MessageButton(
                    Component.literal("switch axis"),
                    () -> this.setState(() -> this.axis = this.axis.opposite())
                ),
                new Padding(Insets.all(5)),
                new Panel(
                    Panel.VANILLA_LIGHT,
                    new Padding(
                        Insets.all(10),
                        new Column(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            Label.literal("that's text"),
                            Label.literal("some more text"),
                            new Padding(
                                Insets.top(5),
                                new CounterTest()
                            )
                        )
                    )
                ),
                new Padding(Insets.all(5)),
                new Panel(
                    Panel.VANILLA_DARK,
                    new Padding(
                        Insets.all(10),
                        new Column(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            Label.literal("that's text"),
                            Label.literal("some more text"),
                            new Padding(
                                Insets.top(5),
                                new CounterTest()
                            )
                        )
                    )
                )
            );
        }
    }
}
