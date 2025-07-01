package io.wispforest.owo.ui.core;

import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import io.wispforest.owo.ui.renderstate.CubeMapElementRenderState;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Surface {

    Surface BLANK = (context, component) -> {};

    Surface PANEL = (context, component) -> {
        context.drawPanel(component.x(), component.y(), component.width(), component.height(), false);
    };

    Surface DARK_PANEL = (context, component) -> {
        context.drawPanel(component.x(), component.y(), component.width(), component.height(), true);
    };

    Surface PANEL_INSET = (context, component) -> {
        NinePatchTexture.draw(OwoUIDrawContext.PANEL_INSET_NINE_PATCH_TEXTURE, context, component);
    };

    Surface VANILLA_TRANSLUCENT = (context, component) -> {
        context.drawGradientRect(
            component.x(), component.y(), component.width(), component.height(),
            0xC0101010, 0xC0101010, 0xD0101010, 0xD0101010
        );
    };

    Surface TOOLTIP = tooltip(null);

    static Surface tooltip(@Nullable Identifier texture) {
        return (context, component) -> {
            TooltipBackgroundRenderer.render(context, component.x() + 4, component.y() + 4, component.width() - 8, component.height() - 8, texture);
        };
    }

    static Surface blur(float quality, float size) {
        return (context, component) -> {
            context.state.addSimpleElement(new BlurQuadElementRenderState(
                new Matrix3x2f(context.getMatrices()),
                new ScreenRect(component.x(), component.y(), component.width(), component.height()),
                context.scissorStack.peekLast(),
                16, quality, size
            ));
        };
    }

    static Surface optionsBackground() {
        return Surface.vanillaPanorama(false).and(Surface.blur(5, 10));
    }

    static Surface vanillaPanorama(boolean alwaysVisible) {
        return panorama(MinecraftClient.getInstance().gameRenderer.getRotatingPanoramaRenderer(), alwaysVisible);
    }

    static Surface panorama(RotatingCubeMapRenderer renderer, boolean alwaysVisible) {
        return (context, component) -> {
            if (!alwaysVisible && MinecraftClient.getInstance().world != null) return;
            context.state.addSpecialElement(new CubeMapElementRenderState(
                renderer, true,
                new ScreenRect(component.x(), component.y(), component.width(), component.height()),
                context.scissorStack.peekLast()
            ));
        };
    }

    static Surface flat(int color) {
        return (context, component) -> context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
    }

    static Surface outline(int color) {
        return (context, component) -> context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), color);
    }

    static Surface tiled(Identifier texture, int textureWidth, int textureHeight) {
        return (context, component) -> {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, component.x(), component.y(), 0, 0, component.width(), component.height(), textureWidth, textureHeight);
        };
    }

    static Surface panelWithInset(int insetWidth) {
        return Surface.PANEL.and((context, component) -> {
            NinePatchTexture.draw(
                OwoUIDrawContext.PANEL_INSET_NINE_PATCH_TEXTURE,
                context,
                component.x() + insetWidth,
                component.y() + insetWidth,
                component.width() - insetWidth * 2,
                component.height() - insetWidth * 2
            );
        });
    }

    void draw(OwoUIDrawContext context, ParentComponent component);

    default Surface and(Surface surface) {
        return (context, component) -> {
            this.draw(context, component);
            surface.draw(context, component);
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
                case "panel-with-inset" -> surface.and(panelWithInset(UIParsing.parseUnsignedInt(child)));
                case "options-background" -> surface.and(optionsBackground());
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
