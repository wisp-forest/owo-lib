package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.w3c.dom.Element;

public class TextureComponent extends BaseComponent {

    protected final Identifier texture;
    protected final int u, v;
    protected final int regionWidth, regionHeight;
    protected final int textureWidth, textureHeight;

    protected TextureComponent(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        this.width = this.regionWidth;
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.height = this.regionHeight;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        RenderSystem.setShaderTexture(0, this.texture);
        Drawer.drawTexture(matrices, this.x, this.y, this.width, this.height, this.u, this.v, this.regionWidth, this.regionHeight, this.textureWidth, this.textureHeight);
    }

    public static TextureComponent parse(Element element) {
        UIParsing.expectAttributes(element, "texture");
        var textureId = UIParsing.parseIdentifier(element.getAttributeNode("texture"));

        int u = 0, v = 0, regionWidth = 0, regionHeight = 0, textureWidth = 256, textureHeight = 256;
        if (element.hasAttribute("u")) {
            u = UIParsing.parseSignedInt(element.getAttributeNode("u"));
        }

        if (element.hasAttribute("v")) {
            v = UIParsing.parseSignedInt(element.getAttributeNode("v"));
        }

        if (element.hasAttribute("region-width")) {
            regionWidth = UIParsing.parseSignedInt(element.getAttributeNode("region-width"));
        }

        if (element.hasAttribute("region-height")) {
            regionHeight = UIParsing.parseSignedInt(element.getAttributeNode("region-height"));
        }

        if (element.hasAttribute("texture-width")) {
            textureWidth = UIParsing.parseSignedInt(element.getAttributeNode("texture-width"));
        }

        if (element.hasAttribute("texture-height")) {
            textureHeight = UIParsing.parseSignedInt(element.getAttributeNode("texture-height"));
        }

        return new TextureComponent(textureId, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
