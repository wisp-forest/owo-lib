package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.object.BlockWidget;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.owo.braid.widgets.object.Viewer;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.window.Window;
import io.wispforest.owo.braid.widgets.window.WindowController;
import io.wispforest.uwu.client.braid.FunnyDragText;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DragArenaTest extends StatefulWidget {
    @Override
    public WidgetState<DragArenaTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<DragArenaTest> {

        private final Set<WindowController> windows = new HashSet<>();

        @Override
        public Widget build(BuildContext context) {
            var elements = new ArrayList<Widget>(List.of(
                new FunnyDragText(Component.literal("me too!")),
                new FunnyDragText(Component.literal("drag me!"))
            ));

            for (var controller : this.windows) {
                elements.add(new Window(
                    true,
                    Component.literal("window " + controller.hashCode()),
                    () -> setState(() -> this.windows.remove(controller)),
                    controller,
                    Size.of(150, 75),
                    new Stack(
                        new Center(
                            new AspectRatio(
                                16d / 9d,
                                new Box(
                                    Color.WHITE.withA(.5),
                                    new Label(Component.literal("16:9 aspect ratio"))
                                )
                            )
                        ),
                        new Align(
                            Alignment.TOP_LEFT,
                            new Column(
                                new Label(Component.literal("a").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://chyz.xyz/box"))))),
                                new MessageButton(Component.literal("window button :o"), () -> setState(() -> controller.toggleCollapsed()))
                            )
                        ),
                        new Align(
                            Alignment.BOTTOM_RIGHT,
                            new Tooltip(
                                Component.literal("tooltip\nhere?"),
                                new Viewer(
                                    transform ->
                                        new ItemStackWidget(
                                            BuiltInRegistries.ITEM.getRandom(RandomSource.create(controller.hashCode())).get().value().getDefaultInstance(),
                                            widget -> widget.transform(transform)
                                        )
                                )
                            )
                        ),
                        new Align(
                            Alignment.BOTTOM_LEFT,
                            new Viewer(
                                transform ->
                                    new BlockWidget(
                                        BuiltInRegistries.BLOCK.getRandom(RandomSource.create(controller.hashCode())).get().value().defaultBlockState(),
                                        transform
                                    )
                            )
                        )
                    )
                ));
            }

            return new Stack(
                new DragArena(elements),
                new Align(
                    Alignment.BOTTOM,
                    new MessageButton(
                        Component.literal("add window"),
                        () -> setState(() -> this.windows.add(new WindowController()))
                    )
                )
            );
        }
    }
}
