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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;

public class LayersTestWidget extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Align(
                Alignment.TOP_LEFT,
                new Padding(
                    Insets.all(15),
                    new MessageButton(
                        Text.literal("layers??"),
                        () -> MinecraftClient.getInstance().getServer().getPlayerManager().getPlayerList().getFirst().kill()
                    )
                )
            ),
            LayerAlignment.atVanillaWidget(
                clickableWidget -> clickableWidget instanceof TexturedButtonWidget,
                AnchorJustification.CENTER_TO_CENTER,
                new Sized(
                    10, 10,
                    new Tooltip(
                        Text.literal("a"),
                        Interactable.primary(
                            () -> MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("braid layer supremacy")),
                            new Box(Color.RED)
                        )
                    )
                )
            ),
            LayerAlignment.atHandledScreenCoordinates(
                136, 63,
                new ItemStackWidget(UwuItems.BRAID.getDefaultStack())
            )
        );
    }
}