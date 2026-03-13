package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Dialog;
import io.wispforest.owo.braid.widgets.Navigator;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.uwu.client.braid.Bikeshed;
import net.minecraft.network.chat.Component;

public class NavigatorTest extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Center(
            new Sized(
                150, 150,
                new Box(
                    Color.BLACK, true,
                    new Padding(
                        Insets.all(1),
                        new Center(new Navigator(new Page1()))
                    )
                )
            )
        );
    }

    public static class Page1 extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Column(
                new MessageButton(
                    Component.literal("page 1"),
                    () -> Navigator.push(context, new BasePage(new Label(Component.literal("page 1"))))
                ),
                new MessageButton(
                    Component.literal("page 2"),
                    () -> Navigator.push(
                        context, new BasePage(
                            new Column(
                                new Label(Component.literal("page 2")),
                                new MessageButton(
                                    Component.literal("popup"),
                                    () -> Navigator.pushOverlay(
                                        context, new Dialog(
                                            new Panel(
                                                Panel.VANILLA_LIGHT,
                                                new Padding(
                                                    Insets.all(5),
                                                    new Sized(
                                                        Size.square(64),
                                                        new Bikeshed()
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
        }
    }

    public static class BasePage extends StatelessWidget {

        public final Widget content;

        public BasePage(Widget content) {
            this.content = content;
        }

        @Override
        public Widget build(BuildContext context) {
            return new Column(
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                this.content,
                new Padding(
                    Insets.top(10),
                    new MessageButton(Component.literal("go back"), () -> Navigator.pop(context))
                )
            );
        }
    }
}
