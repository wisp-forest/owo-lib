package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.OwoUIRenderLayers;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.SpriteUtilInvoker;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import org.w3c.dom.Element;

import java.util.Map;

public class SpriteComponent extends BaseComponent {

    protected final Sprite sprite;
    protected boolean blend = false;

    protected SpriteComponent(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.sprite.getContents().getWidth();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.sprite.getContents().getHeight();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        SpriteUtilInvoker.markSpriteActive(this.sprite);

        if (this.blend) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }

        context.drawSpriteStretched(identifier -> OwoUIRenderLayers.getGuiTextured(identifier, this.blend), this.sprite, this.x, this.y, this.width, this.height);

        if (this.blend) {
            RenderSystem.disableBlend();
        }
    }

    public SpriteComponent blend(boolean blend) {
        this.blend = blend;
        return this;
    }

    public boolean blend() {
        return this.blend;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "blend", UIParsing::parseBool, this::blend);
    }
    public static SpriteComponent parse(Element element) {
        UIParsing.expectAttributes(element, "atlas", "sprite");

        var atlas = UIParsing.parseIdentifier(element.getAttributeNode("atlas"));
        var sprite = UIParsing.parseIdentifier(element.getAttributeNode("sprite"));

        return Components.sprite(new SpriteIdentifier(atlas, sprite));
    }
}
