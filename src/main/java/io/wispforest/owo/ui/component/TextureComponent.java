package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.BaseComponent;
import io.wispforest.owo.ui.Drawer;
import io.wispforest.owo.ui.definitions.Sizing;
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

        if (element.hasAttribute("regionWidth")) {
            regionWidth = UIParsing.parseSignedInt(element.getAttributeNode("regionWidth"));
        }

        if (element.hasAttribute("regionHeight")) {
            regionHeight = UIParsing.parseSignedInt(element.getAttributeNode("regionHeight"));
        }

        if (element.hasAttribute("textureWidth")) {
            textureWidth = UIParsing.parseSignedInt(element.getAttributeNode("textureWidth"));
        }

        if (element.hasAttribute("textureHeight")) {
            textureHeight = UIParsing.parseSignedInt(element.getAttributeNode("textureHeight"));
        }

        return new TextureComponent(textureId, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
