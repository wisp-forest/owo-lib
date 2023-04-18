package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.client.OwoClient;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.OwoNinePatchRenderers;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Surface {

    Surface PANEL = (matrices, component) -> {
        Drawer.drawPanel(matrices, component.x(), component.y(), component.width(), component.height(), false);
    };

    Surface DARK_PANEL = (matrices, component) -> {
        Drawer.drawPanel(matrices, component.x(), component.y(), component.width(), component.height(), true);
    };

    Surface PANEL_INSET = OwoNinePatchRenderers.PANEL_INSET::draw;

    Surface VANILLA_TRANSLUCENT = (matrices, component) -> {
        Drawer.drawGradientRect(matrices,
                component.x(), component.y(), component.width(), component.height(),
                0xC0101010, 0xC0101010, 0xD0101010, 0xD0101010
        );
    };

    Surface OPTIONS_BACKGROUND = (matrices, component) -> {
        RenderSystem.setShaderTexture(0, Drawer.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(64 / 255f, 64 / 255f, 64 / 255f, 1);
        Drawer.drawTexture(matrices, component.x(), component.y(), 0, 0, component.width(), component.height(), 32, 32);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    };

    Surface TOOLTIP = (matrices, component) -> {
        var buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        TooltipBackgroundRenderer.render(Drawer::fillGradient, matrices.peek().getPositionMatrix(), buffer, component.x() + 4, component.y() + 4, component.width() - 8, component.height() - 8, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator.getInstance().draw();
    };

    static Surface blur(float quality, float size) {
        return (matrices, component) -> {
            var buffer = Tessellator.getInstance().getBuffer();
            var matrix = matrices.peek().getPositionMatrix();

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            buffer.vertex(matrix, component.x(), component.y(), 0).next();
            buffer.vertex(matrix, component.x(), component.y() + component.height(), 0).next();
            buffer.vertex(matrix, component.x() + component.width(), component.y() + component.height(), 0).next();
            buffer.vertex(matrix, component.x() + component.width(), component.y(), 0).next();

            OwoClient.BLUR_PROGRAM.setParameters(16, quality, size);
            OwoClient.BLUR_PROGRAM.use();
            Tessellator.getInstance().draw();
        };
    }

    Surface BLANK = (matrices, component) -> {};

    static Surface flat(int color) {
        return (matrices, component) -> Drawer.fill(matrices, component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
    }

    static Surface outline(int color) {
        return (matrices, component) -> Drawer.drawRectOutline(matrices, component.x(), component.y(), component.width(), component.height(), color);
    }

    static Surface tiled(Identifier texture, int textureWidth, int textureHeight) {
        return (matrices, component) -> {
            RenderSystem.setShaderTexture(0, texture);
            Drawer.drawTexture(matrices, component.x(), component.y(), 0, 0, component.width(), component.height(), textureWidth, textureHeight);
        };
    }

    void draw(MatrixStack matrices, ParentComponent component);

    default Surface and(Surface surface) {
        return (matrices, component) -> {
            this.draw(matrices, component);
            surface.draw(matrices, component);
        };
    }

    static Surface parse(Element surfaceElement) {
        var children = UIParsing.<Element>allChildrenOfType(surfaceElement, Node.ELEMENT_NODE);
        var surface = BLANK;

        for (var child : children) {
            surface = switch (child.getNodeName()) {
                case "panel" -> surface.and(child.getAttribute("dark").equalsIgnoreCase("true")
                        ? DARK_PANEL
                        : PANEL);
                case "tiled" -> {
                    UIParsing.expectAttributes(child, "texture-width", "texture-height");
                    yield surface.and(tiled(
                            UIParsing.parseIdentifier(child),
                            UIParsing.parseUnsignedInt(child.getAttributeNode("texture-width")),
                            UIParsing.parseUnsignedInt(child.getAttributeNode("texture-height")))
                    );
                }
                case "blur" -> {
                    UIParsing.expectAttributes(child, "size", "quality");
                    yield surface.and(blur(
                            UIParsing.parseFloat(child.getAttributeNode("quality")),
                            UIParsing.parseFloat(child.getAttributeNode("size"))
                    ));
                }
                case "options-background" -> surface.and(OPTIONS_BACKGROUND);
                case "vanilla-translucent" -> surface.and(VANILLA_TRANSLUCENT);
                case "panel-inset" -> surface.and(PANEL_INSET);
                case "tooltip" -> surface.and(TOOLTIP);
                case "outline" -> surface.and(outline(Color.parseAndPack(child)));
                case "flat" -> surface.and(flat(Color.parseAndPack(child)));
                default -> throw new UIModelParsingException("Unknown surface type '" + child.getNodeName() + "'");
            };
        }

        return surface;
    }
}
