package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.splitpane.MultiSplitPane;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SplitPaneTest extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Column(
            MainAxisAlignment.START,
            CrossAxisAlignment.CENTER,
            new Sized(
                250.0,
                200.0,
                new MultiSplitPane(
                    LayoutAxis.HORIZONTAL,
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    List.of(
                        new Box(
                            Color.mix(.5, Color.GREEN, new Color(0)),
                            new Label(Component.literal("text here"))
                        ),
                        new Box(
                            Color.mix(.5, Color.GREEN, new Color(0)),
                            new Label(Component.literal("text here"))
                        ),
                        new Box(
                            Color.mix(.5, Color.GREEN, new Color(0)),
                            new Label(Component.literal("text here"))
                        ),
                        new Box(
                            Color.mix(.5, Color.GREEN, new Color(0)),
                            new Label(Component.literal("text here"))
                        )
                    )
                )
            )
        );
    }
}
