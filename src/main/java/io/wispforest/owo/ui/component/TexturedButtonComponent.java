package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * @deprecated Replaced by {@link io.wispforest.owo.ui.component.ButtonComponent.Renderer#texture(Identifier, int, int, int, int)}
 */
@Deprecated(forRemoval = true)
public class TexturedButtonComponent extends ButtonWidget {

    protected final Identifier texture;
    protected final int textureWidth, textureHeight;
    protected final int u, v;

    protected boolean textShadow = true;

    protected TexturedButtonComponent(Identifier texture, int width, int height, int u, int v, int textureWidth, int textureHeight, Text message, PressAction onPress) {
        super(0, 0, width, height, message, onPress);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);

        int renderV = this.v;
        if (!this.active) {
            renderV += this.height * 2;
        } else if (this.isHovered()) {
            renderV += this.height;
        }

        RenderSystem.enableDepthTest();
        Drawer.drawTexture(matrices, this.x, this.y, this.u, renderV, this.width, this.height, this.textureWidth, this.textureHeight);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        if (this.textShadow) {
            Drawer.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFF);
        } else {
            textRenderer.draw(matrices, this.getMessage(), this.x + this.width / 2f - textRenderer.getWidth(this.getMessage()) / 2f, this.y + (this.height - 8) / 2f, 0xFFFFFF);
        }

        if (this.hovered) this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text-shadow", UIParsing::parseBool, this::textShadow);
    }

    public TexturedButtonComponent textShadow(boolean textShadow) {
        this.textShadow = textShadow;
        return this;
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    public static TexturedButtonComponent parse(Element element) {
        UIParsing.expectAttributes(element, "texture", "width", "height");
        var textureId = UIParsing.parseIdentifier(element.getAttributeNode("texture"));

        int width = UIParsing.parseSignedInt(element.getAttributeNode("width"));
        int height = UIParsing.parseSignedInt(element.getAttributeNode("height"));

        int u = 0, v = 0, textureWidth = 256, textureHeight = 256;
        if (element.hasAttribute("u")) {
            u = UIParsing.parseSignedInt(element.getAttributeNode("u"));
        }

        if (element.hasAttribute("v")) {
            v = UIParsing.parseSignedInt(element.getAttributeNode("v"));
        }

        if (element.hasAttribute("texture-width")) {
            textureWidth = UIParsing.parseSignedInt(element.getAttributeNode("texture-width"));
        }

        if (element.hasAttribute("texture-height")) {
            textureHeight = UIParsing.parseSignedInt(element.getAttributeNode("texture-height"));
        }

        return Components.texturedButton(textureId, Text.empty(), width, height, u, v, textureWidth, textureHeight, button -> {});
    }
}
