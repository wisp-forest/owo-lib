package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;

public class SpriteComponent extends BaseComponent {

    protected final Sprite sprite;

    protected SpriteComponent(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        this.width = this.sprite.method_45851().method_45807();
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.height = this.sprite.method_45851().method_45815();
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        RenderSystem.setShaderTexture(0, this.sprite.method_45852());
        Drawer.drawSprite(matrices, this.x, this.y, 0, this.width, this.height, this.sprite);
    }

    public static SpriteComponent parse(Element element) {
        UIParsing.expectAttributes(element, "atlas", "sprite");

        var atlas = UIParsing.parseIdentifier(element.getAttributeNode("atlas"));
        var sprite = UIParsing.parseIdentifier(element.getAttributeNode("sprite"));

        return Components.sprite(new SpriteIdentifier(atlas, sprite));
    }
}
