package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;

public class LavaLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new SpriteWidget(
                new Material(
                    TextureAtlas.LOCATION_BLOCKS,
                    Identifier.withDefaultNamespace("block/lava_flow") // the lava flow sprite is 32x32, which is smaller than
                )                                                // the 64x64 braid logo
            ),
            new StackBase(                                       // but by making the logo the base, the lava will be
                new BraidLogo()                                  // force to have the same size, effectively using it
            )                                                    // as a backdrop for the logo
        );
    }
}