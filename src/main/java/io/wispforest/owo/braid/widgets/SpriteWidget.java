package io.wispforest.owo.braid.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.OwoUIRenderLayers;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.OptionalDouble;

public class SpriteWidget extends LeafInstanceWidget {

    public final SpriteIdentifier spriteIdentifier;
    public final boolean blend;

    public SpriteWidget(SpriteIdentifier spriteIdentifier, boolean blend) {
        this.spriteIdentifier = spriteIdentifier;
        this.blend = blend;
    }

    @Override
    public LeafWidgetInstance<SpriteWidget> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<SpriteWidget> {

        protected static final Identifier SPRITE_ATLAS_ID = Identifier.of("textures/atlas/gui.png");
        protected Sprite sprite;

        public Instance(SpriteWidget widget) {
            super(widget);
        }

        @Override
        public void setWidget(SpriteWidget widget) {
            if (this.widget.spriteIdentifier.equals(widget.spriteIdentifier)) return;

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sprite = this.widget.spriteIdentifier.getAtlasId().equals(SPRITE_ATLAS_ID)
                ? this.host().client().getGuiAtlasManager().getSprite(this.widget.spriteIdentifier.getTextureId())
                : this.widget.spriteIdentifier.getSprite();

            var size = Size.of(
                this.sprite.getContents().getWidth(),
                this.sprite.getContents().getHeight()
            ).constrained(constraints);

            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.sprite.getContents().getWidth();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.sprite.getContents().getHeight();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            if (this.widget.blend) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }

            ctx.drawSpriteStretched(
                identifier -> OwoUIRenderLayers.getGuiTextured(identifier, this.widget.blend),
                this.sprite,
                0,
                0,
                (int) this.transform.width(),
                (int) this.transform.height()
            );

            if (this.widget.blend) {
                RenderSystem.disableBlend();
            }
        }
    }
}
