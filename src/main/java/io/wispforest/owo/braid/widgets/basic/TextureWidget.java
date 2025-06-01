package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.OwoUIRenderLayers;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TextureWidget extends OptionalChildInstanceWidget {

    public final Identifier texture;
    public final int u, v;
    public final int regionWidth, regionHeight;
    public final int textureWidth, textureHeight;

    public final boolean stretch;

    public TextureWidget(
        Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean stretch, @Nullable Widget child
    ) {
        super(child);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.stretch = stretch;
    }

    public TextureWidget(
        Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean stretch
    ) {
        this(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight, stretch, null);
    }

    @Override
    public OptionalChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends OptionalChildWidgetInstance<TextureWidget> {

        public Instance(TextureWidget widget) {
            super(widget);
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            var matrices = ctx.getMatrices();

            if (widget.stretch) {
                matrices.push();
                matrices.scale((int) this.transform.width() / (float) widget.regionWidth, (int) this.transform.height() / (float) widget.regionHeight, 1);
            }

            ctx.drawTexture(
                identifier -> OwoUIRenderLayers.getGuiTextured(identifier, true),
                widget.texture,
                0,
                0,
                widget.u,
                widget.v,
                widget.stretch ? widget.regionWidth : (int) this.transform.toSize().width(),
                widget.stretch ? widget.regionHeight : (int) this.transform.toSize().height(),
                widget.regionWidth,
                widget.regionHeight,
                widget.textureWidth,
                widget.textureHeight
            );

            if (widget.stretch) matrices.pop();

            super.draw(ctx);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            if (this.child == null) {
                this.transform.setSize(constraints.maxSize());
            } else {
                this.sizeToChild(constraints, this.child);
            }
        }
    }
}
