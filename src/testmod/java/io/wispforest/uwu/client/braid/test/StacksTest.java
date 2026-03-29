package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import net.minecraft.network.chat.Component;

public class StacksTest extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Center(
            new Row(
                new Stack(
                    new Panel(Panel.VANILLA_LIGHT),
                    new StackBase(new Sized(100, 100, new Padding(Insets.none()))),
                    new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Component.literal("based corner text"))
                ),
                new Padding(Insets.horizontal(20)),
                new Stack(
                    new Sized(100, 100, new Panel(Panel.VANILLA_LIGHT)),
                    new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Component.literal("failed corner text"))
                ),
                new Padding(Insets.horizontal(20)),
                new IntrinsicWidth(
                    new IntrinsicHeight(
                        new Stack(
                            new Sized(100, 100, new Panel(Panel.VANILLA_LIGHT)),
                            new Label(new LabelStyle(Alignment.BOTTOM_RIGHT, null, null, null), true, Component.literal("intrinsic corner text"))
                        )
                    )
                )
            )
        );
    }
}
