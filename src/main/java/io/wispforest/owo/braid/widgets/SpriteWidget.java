package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;

import java.util.OptionalDouble;

public class SpriteWidget extends LeafInstanceWidget {

    public static final Identifier GUI_ATLAS_ID = Identifier.withDefaultNamespace("textures/atlas/gui.png");

    public final Material spriteIdentifier;

    public SpriteWidget(Material spriteIdentifier) {
        this.spriteIdentifier = spriteIdentifier;
    }

    public SpriteWidget(Identifier spriteIdentifier) {
        this.spriteIdentifier = new Material(GUI_ATLAS_ID, spriteIdentifier);
    }

    @Override
    public LeafWidgetInstance<SpriteWidget> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<SpriteWidget> {

        protected TextureAtlasSprite sprite;

        public Instance(SpriteWidget widget) {
            super(widget);
        }

        @Override
        public void setWidget(SpriteWidget widget) {
            if (this.widget.spriteIdentifier.equals(widget.spriteIdentifier)) return;

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        protected TextureAtlasSprite findSprite() {
            try {
                this.sprite = Minecraft.getInstance().getAtlasManager().get(this.widget.spriteIdentifier);
            } catch (IllegalArgumentException ignored) {
                this.sprite = Minecraft.getInstance().getAtlasManager().get(new Material(GUI_ATLAS_ID, TextureManager.INTENTIONAL_MISSING_TEXTURE));
            }

            return this.sprite;
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sprite = this.findSprite();

            var size = Size.of(
                this.sprite.contents().width(),
                this.sprite.contents().height()
            ).constrained(constraints);

            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.findSprite().contents().width();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.findSprite().contents().height();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        public void draw(BraidGraphics graphics) {
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.sprite,
                0,
                0,
                (int) this.transform.width(),
                (int) this.transform.height()
            );
        }
    }
}
