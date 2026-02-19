package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.basic.TextureWidget;
import net.minecraft.resources.Identifier;

public class BraidLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Sized(
            64, 64,
            new TextureWidget(
                TEXTURE_ID,
                TextureWidget.Wrap.STRETCH,
                Color.WHITE
            )
        );
    }

    private static final Identifier TEXTURE_ID = Owo.id("textures/gui/braid_logo.png");
}