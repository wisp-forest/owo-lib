package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class LayersTestWidget extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Align(
            Alignment.TOP_LEFT,
            new Padding(
                Insets.all(15),
                new MessageButton(
                    Text.literal("layers??"),
                    () -> MinecraftClient.getInstance().getServer().getPlayerManager().getPlayerList().getFirst().kill(MinecraftClient.getInstance().getServer().getOverworld())
                )
            )
        );
    }
}
