package io.wispforest.owo.braid.widgets.basic;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.InstanceHost;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.TextureSizeLookup;
import io.wispforest.owo.mixin.ui.access.AbstractTextureAccessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

public class TextureWidget extends OptionalChildInstanceWidget {

    public final Identifier texture;
    public final Wrap wrap;
    public final Filter filter;
    public final Color color;

    public TextureWidget(Identifier texture, Wrap wrap, Filter filter, Color color, @Nullable Widget child) {
        super(child);
        this.texture = texture;
        this.wrap = wrap;
        this.filter = filter;
        this.color = color;
    }

    public TextureWidget(Identifier texture, Wrap wrap, Color color, @Nullable Widget child) {
        this(texture, wrap, Filter.TEXTURE_DEFAULT, color, child);
    }

    public TextureWidget(Identifier texture, Wrap wrap, Filter filter, Color color) {
        this(texture, wrap, filter, color, null);
    }

    public TextureWidget(Identifier texture, Wrap wrap, Color color) {
        this(texture, wrap, Filter.TEXTURE_DEFAULT, color);
    }

    @Override
    public OptionalChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    // ---

    public enum Wrap {
        NONE, STRETCH, REPEAT
    }

    public enum Filter {
        TEXTURE_DEFAULT, NEAREST, LINEAR;
    }

    // ---

    public static class Instance extends OptionalChildWidgetInstance<TextureWidget> {

        private @Nullable Size textureSize;

        public Instance(TextureWidget widget) {
            super(widget);
        }

        @Override
        public void attachHost(InstanceHost host) {
            super.attachHost(host);
            this.refreshTextureSize();
        }

        @Override
        public void setWidget(TextureWidget widget) {
            super.setWidget(widget);
            this.refreshTextureSize();
        }

        private void refreshTextureSize() {
            this.textureSize = TextureSizeLookup.sizeOf(widget.texture);
        }

        private double imageAspectRatio() {
            //noinspection DataFlowIssue
            return this.textureSize.width() / this.textureSize.height();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            if (this.child == null) {
                var size = this.textureSize != null
                    ? AspectRatio.applyAspectRatio(constraints, this.textureSize)
                    : constraints.maxFiniteOrMinSize();

                this.transform.setSize(size);
            } else {
                this.sizeToChild(constraints, this.child);
            }
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.child != null
                ? this.child.getIntrinsicWidth(height)
                : this.textureSize != null
                    ? Double.isFinite(height) ? height * this.imageAspectRatio() : this.textureSize.width()
                    : 0;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.child != null
                ? this.child.getIntrinsicHeight(width)
                : this.textureSize != null
                    ? Double.isFinite(width) ? width / this.imageAspectRatio() : this.textureSize.height()
                    : 0;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.child != null ? this.child.getBaselineOffset() : OptionalDouble.empty();
        }

        @Override
        public void draw(BraidDrawContext ctx) {
            var matrices = ctx.getMatrices();
            var stretch = this.widget.wrap == Wrap.STRETCH;

            var textureWidth = (int) (this.textureSize != null ? this.textureSize.width() : this.transform.width());
            var textureHeight = (int) (this.textureSize != null ? this.textureSize.height() : this.transform.height());

            var quadWidth = (int) (this.widget.wrap != Wrap.REPEAT ? textureWidth : this.transform.width());
            var quadHeight = (int) (this.widget.wrap != Wrap.REPEAT ? textureHeight : this.transform.height());

            if (stretch) {
                matrices.push();
                matrices.scale((int) this.transform.width() / (float) textureWidth, (int) this.transform.height() / (float) textureHeight, 1);
            }

            var texture = this.host().client().getTextureManager().getTexture(this.widget.texture);
            var textureBilinear = ((AbstractTextureAccessor) texture).owo$getBilinear();
            var textureMipmap = ((AbstractTextureAccessor) texture).owo$getMipmap();

            switch (this.widget.filter) {
                case NEAREST -> texture.setFilter(false, textureMipmap);
                case LINEAR -> texture.setFilter(true, textureMipmap);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            var shaderColor = RenderSystem.getShaderColor().clone();
            RenderSystem.setShaderColor((float) this.widget.color.r, (float) this.widget.color.g, (float) this.widget.color.b, (float) this.widget.color.a);

            ctx.drawTexture(
                this.widget.texture,
                0, 0, 0, 0,
                quadWidth, quadHeight,
                textureWidth, textureHeight
            );

            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
            RenderSystem.disableBlend();

            texture.setFilter(textureBilinear, textureMipmap);

            if (stretch) {
                matrices.pop();
            }

            super.draw(ctx);
        }
    }
}
