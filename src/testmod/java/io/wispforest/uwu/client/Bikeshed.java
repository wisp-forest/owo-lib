package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.basic.TextureWidget;
import net.minecraft.resources.Identifier;

public class Bikeshed extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Sized(
            256,
            256,
            new TextureWidget(
                Identifier.fromNamespaceAndPath("uwu", "textures/gui/bikeshed.png"),
                TextureWidget.Wrap.STRETCH, Color.WHITE
            )
        );
    }
}
