package io.wispforest.owo.ui.core;

import io.wispforest.owo.mixin.ScreenAccessor;
import io.wispforest.owo.shader.OwoBlurRenderer;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Surface {

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

    Surface OPTIONS_BACKGROUND = Surface.panorama(ScreenAccessor.owo$ROTATING_PANORAMA_RENDERER(), false)
        .and(Surface.blur(5, 10));

    Surface TOOLTIP = tooltip(null);

    static Surface tooltip(@Nullable Identifier texture) {
        return (context, component) -> {
            TooltipBackgroundRenderer.render(context, component.x() + 4, component.y() + 4, component.width() - 8, component.height() - 8, 0, texture);
        };
    }

    static Surface blur(float quality, float size) {
        return (context, component) -> {
            OwoBlurRenderer.drawBlur(context, component, 16, quality, size);
        };
    }

    static Surface vanillaPanorama(boolean alwaysVisible) {
        return panorama(new RotatingCubeMapRenderer(ScreenAccessor.owo$PANORAMA_RENDERER()), alwaysVisible);
    }

    static Surface panorama(RotatingCubeMapRenderer renderer, boolean alwaysVisible) {
        return (context, component) -> {
            if (!alwaysVisible && MinecraftClient.getInstance().world != null) return;

            var client = MinecraftClient.getInstance();

            var window = client.getWindow();
            var scale = window.getScaleFactor();

            var x = component.x();
            var y = component.y();
            var width = component.width();
            var height = component.height();

            OwoUIDrawContext.viewportOverride = new ScreenRect(
                (int) (x * scale),
                (int) (window.getFramebufferHeight() - (y * scale) - height * scale),
                MathHelper.clamp((int) (width * scale), 0, window.getFramebufferWidth()),
                MathHelper.clamp((int) (height * scale), 0, window.getFramebufferHeight())
            );

            try {
                var delta = client.getRenderTickCounter().getDynamicDeltaTicks();
                renderer.render(context, width, height, 1.0F, delta);
                context.draw();
            } finally {
                OwoUIDrawContext.viewportOverride = null;
            }
        };
    }

    Surface BLANK = (context, component) -> {};

    static Surface flat(int color) {
        return (context, component) -> context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
    }

    static Surface outline(int color) {
        return (context, component) -> context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), color);
    }

    static Surface tiled(Identifier texture, int textureWidth, int textureHeight) {
        return (context, component) -> {
            context.drawTexture(RenderLayer::getGuiTextured, texture, component.x(), component.y(), 0, 0, component.width(), component.height(), textureWidth, textureHeight);
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
