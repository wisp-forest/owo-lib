package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.OptionalDouble;

public class SpriteWidget extends LeafInstanceWidget {

    public static final Identifier GUI_ATLAS_ID = Identifier.of("textures/atlas/gui.png");

    public final SpriteIdentifier spriteIdentifier;

    public SpriteWidget(SpriteIdentifier spriteIdentifier) {
        this.spriteIdentifier = spriteIdentifier;
    }

    public SpriteWidget(Identifier spriteIdentifier) {
        this.spriteIdentifier = new SpriteIdentifier(GUI_ATLAS_ID, spriteIdentifier);
    }

    @Override
    public LeafWidgetInstance<SpriteWidget> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<SpriteWidget> {

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

        protected Sprite findSprite() {
            return this.sprite = this.widget.spriteIdentifier.getAtlasId().equals(GUI_ATLAS_ID)
                ? this.host().client().getGuiAtlasManager().getSprite(this.widget.spriteIdentifier.getTextureId())
                : this.widget.spriteIdentifier.getSprite();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sprite = this.findSprite();

            var size = Size.of(
                this.sprite.getContents().getWidth(),
                this.sprite.getContents().getHeight()
            ).constrained(constraints);

            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.findSprite().getContents().getWidth();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.findSprite().getContents().getHeight();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        public void draw(BraidDrawContext ctx) {
            ctx.drawSprite(
                0,
                0,
                0,
                (int) this.transform.width(),
                (int) this.transform.height(),
                this.sprite
            );
        }
    }
}
