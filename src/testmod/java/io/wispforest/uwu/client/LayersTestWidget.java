package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.layers.AnchorJustification;
import io.wispforest.owo.braid.util.layers.LayerAlignment;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.uwu.items.UwuItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;

public class LayersTestWidget extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Align(
                Alignment.TOP_LEFT,
                new Padding(
                    Insets.all(15),
                    new MessageButton(
                        Component.literal("layers??"),
                        () -> Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().getFirst().kill(Minecraft.getInstance().getSingleplayerServer().overworld())
                    )
                )
            ),
            LayerAlignment.atVanillaWidget(
                clickableWidget -> clickableWidget instanceof ImageButton,
                AnchorJustification.CENTER_TO_CENTER,
                new Sized(
                    10, 10,
                    new Tooltip(
                        Component.literal("a"),
                        Interactable.primary(
                            () -> Minecraft.getInstance().gui.getChat().addMessage(Component.literal("braid layer supremacy")),
                            new Box(Color.RED)
                        )
                    )
                )
            ),
            LayerAlignment.atContainerScreenCoordinates(
                136, 63,
                new ItemStackWidget(UwuItems.BRAID.getDefaultInstance())
            )
        );
    }
}