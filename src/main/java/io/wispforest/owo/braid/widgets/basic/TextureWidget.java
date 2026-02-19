package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.instance.InstanceHost;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.resources.Identifier;
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
            var texture = this.host().client().getTextureManager().getTexture(widget.texture).getTexture();
            var newTextureSize = Size.of(
                texture.getWidth(0),
                texture.getHeight(0)
            );

            if (!newTextureSize.equals(this.textureSize)) {
                this.markNeedsLayout();
            }

            this.textureSize = newTextureSize;
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
        public void draw(BraidGraphics graphics) {
            var matrices = graphics.pose();
            var stretch = this.widget.wrap == Wrap.STRETCH;

            var textureWidth = (int) (this.textureSize != null ? this.textureSize.width() : this.transform.width());
            var textureHeight = (int) (this.textureSize != null ? this.textureSize.height() : this.transform.height());

            var quadWidth = (int) (this.widget.wrap != Wrap.REPEAT ? textureWidth : this.transform.width());
            var quadHeight = (int) (this.widget.wrap != Wrap.REPEAT ? textureHeight : this.transform.height());

            if (stretch) {
                matrices.pushMatrix();
                matrices.scale((int) this.transform.width() / (float) textureWidth, (int) this.transform.height() / (float) textureHeight);
            }

            var pipeline = switch (this.widget.filter) {
                case TEXTURE_DEFAULT -> BraidRenderPipelines.TEXTURED_DEFAULT;
                case NEAREST -> BraidRenderPipelines.TEXTURED_NEAREST;
                case LINEAR -> BraidRenderPipelines.TEXTURED_BILINEAR;
            };

            graphics.blit(
                pipeline,
                this.widget.texture,
                0, 0, 0, 0,
                quadWidth, quadHeight,
                textureWidth, textureHeight,
                this.widget.color.argb()
            );

            if (stretch) {
                matrices.popMatrix();
            }

            super.draw(graphics);
        }
    }
}
