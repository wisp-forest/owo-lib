package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Marquee;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.util.Wisdom;
import io.wispforest.uwu.client.braid.Bikeshed;
import io.wispforest.uwu.client.braid.GayAmogus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Comparator;

public class TextTest extends StatefulWidget {
    @Override
    public WidgetState<TextTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<TextTest> {

        @Override
        public Widget build(BuildContext context) {
            var wisdomText = Component.literal(String.join(" ", Wisdom.ALL_THE_WISDOM));

            return new DragArena(
                new Window(
                    false,
                    Component.literal("ellipsis moment"),
                    null, null,
                    Size.square(100),
                    new Label(
                        new LabelStyle(Alignment.TOP_LEFT, null, null, null),
                        true, Label.Overflow.ELLIPSIS,
                        wisdomText
                    )
                ),
                new Window(
                    false,
                    Component.literal("clip moment"),
                    null, null,
                    Size.square(100),
                    new Label(
                        new LabelStyle(Alignment.TOP_LEFT, null, null, null),
                        true, Label.Overflow.CLIP,
                        wisdomText
                    )
                ),
                new Window(
                    false,
                    Component.literal("marquee moment"),
                    null, null,
                    Size.square(100),
                    new Column(
                        Wisdom.ALL_THE_WISDOM.stream()
                            .sorted(Comparator.comparingInt(value -> Minecraft.getInstance().font.width(value)))
                            .map(s -> new Marquee(
                                new Label(
                                    new LabelStyle(Alignment.TOP_LEFT, null, null, null),
                                    true, Label.Overflow.CLIP,
                                    Component.literal(s)
                                )
                            )).toList()
                    )
                ),
                new Window(
                    false,
                    Component.literal("cursed marquee moment"),
                    null, null,
                    Size.square(100),
                    new Marquee(
                        widget -> widget.pauseWhileHovered(false),
                        new Bikeshed()
                    )
                ),
                new Window(
                    false,
                    Component.literal("dvd moment"),
                    null, null,
                    Size.square(100),
                    new Center(
                        new Marquee(
                            widget -> widget.axis(LayoutAxis.VERTICAL),
                            new Marquee(
                                new Panel(
                                    Identifier.fromNamespaceAndPath("uwu", "contributors_panel"),
                                    new Sized(
                                        32 * 4,
                                        32 * 4,
                                        new Marquee(
                                            widget -> widget
                                                .easing(Easing.LINEAR)
                                                .minDuration(0)
                                                .durationPerPixel(10)
                                                .pauseTime(0),
                                            new Marquee(
                                                widget -> widget
                                                    .easing(Easing.LINEAR)
                                                    .minDuration(0)
                                                    .durationPerPixel(15)
                                                    .pauseTime(0)
                                                    .axis(LayoutAxis.VERTICAL),
                                                new Align(
                                                    Alignment.TOP_LEFT,
                                                    new Padding(
                                                        Insets.all(32 * 3),
                                                        new GayAmogus(
                                                            8
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
                )
            );
        }
    }
}
