package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.owoui.OwoUIWidget;
import io.wispforest.owo.braid.widgets.splitpane.MultiSplitPane;
import io.wispforest.owo.braid.widgets.vanilla.VanillaWidget;
import io.wispforest.owo.ui.component.BraidComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.uwu.client.braid.TestSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

import java.util.List;

public class VanillaTest extends StatefulWidget {
    @Override
    public WidgetState<VanillaTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<VanillaTest> {
        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                250.0,
                250.0,
                new Column(
                    new VanillaWidget<>(
                        Size.of(250, 20),
                        () ->
                            net.minecraft.client.gui.components.Checkbox.builder(
                                Component.literal("Checkbox"),
                                Minecraft.getInstance().font
                            ).build()
                    ),
                    new VanillaWidget<>(
                        Size.of(250, 20),
                        () -> {
                            var widget = new EditBox(
                                Minecraft.getInstance().font,
                                0, 0, 100, 20,
                                Component.literal("Text Field")
                            );
                            widget.setHint(Component.literal("when the vanilla widget is no longer better than the braid widget"));
                            return widget;
                        }
                    ),
                    new OwoUIWidget(
                        () -> {
                            var root = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
                            root.child(
                                UIComponents.button(
                                    Component.literal("A very very cool button"),
                                    button -> Minecraft
                                        .getInstance()
                                        .getSoundManager()
                                        .play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE, RandomSource.create().nextFloat() * 2f))
                                )
                            ).child(
                                new BraidComponent(
                                    new Column(
                                        new MessageButton(
                                            Component.literal("amogus"),
                                            () -> Minecraft
                                                .getInstance()
                                                .getSoundManager()
                                                .play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_BREAK, RandomSource.create().nextFloat() * 2f))
                                        ),
                                        // idk why this needs to be here but if it's not the split pane becomes
                                        // infinity sized
                                        new Sized(
                                            180, 180,
                                            new MultiSplitPane(
                                                LayoutAxis.HORIZONTAL,
                                                MainAxisAlignment.START,
                                                CrossAxisAlignment.CENTER,
                                                List.of(
                                                    new Box(
                                                        Color.mix(.5, Color.GREEN, new Color(0)),
                                                        new Label(Component.literal("no way is"))
                                                    ),
                                                    new Box(
                                                        Color.mix(.5, Color.GREEN, new Color(0)),
                                                        new Label(Component.literal("that braid"))
                                                    ),
                                                    new Box(
                                                        Color.mix(.5, Color.GREEN, new Color(0)),
                                                        new Label(Component.literal("inside owoui"))
                                                    ),
                                                    new Box(
                                                        Color.mix(.5, Color.GREEN, new Color(0)),
                                                        new Label(Component.literal("inside braid?"))
                                                    )
                                                )
                                            )
                                        )
                                    )
                                ).sizing(Sizing.fixed(200))
                            ).allowOverflow(true);
                            return root;
                        }
                    )
                )
            );
        }
    }
}
