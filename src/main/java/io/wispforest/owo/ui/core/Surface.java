package io.wispforest.owo.ui.core;

import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import io.wispforest.owo.ui.renderstate.CubeMapElementRenderState;
import io.wispforest.owo.ui.util.NinePatchTexture;
import io.wispforest.owo.ui.util.WrappedMatrix2fStack;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

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
        return Surface.vanillaPanorama(false)
            .and(Surface.blur(5, 10))
            .and((context, component) -> {
                var texture = MinecraftClient.getInstance().world == null ? Screen.MENU_BACKGROUND_TEXTURE : Identifier.ofVanilla("textures/gui/inworld_menu_background.png");
                context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, component.x(), component.y(), 0.0F, 0.0F, component.width(), component.height(), 32, 32);
            });
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
        return outline(color, 0);
    }

    static Surface outline(int color, int insetWidth) {
        return (context, component) ->
                context.drawRectOutline(
                component.x() + insetWidth,
                component.y() + insetWidth,
                component.width() - insetWidth * 2,
                component.height() - insetWidth * 2,
                color
        );
    }

    static Surface partialOutline(int color, OutlineSide ...targetedSides) {
        return partialOutline(color, 0, targetedSides);
    }

    static Surface partialOutline(int color, int insetWidth, OutlineSide ...targetedSides) {
        return partialOutline(color, insetWidth, false, targetedSides);
    }

    List<Color> DEBUG_COLORS = List.of(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE);

    List<Color> TRANSPARENT_DEBUG_COLORS = Util.make(new ArrayList<>(), colors -> {
        colors.add(new Color(0, 0, 0, 0.5f)); // BLACK
        colors.add(new Color(1, 0, 0, 0.5f)); // RED
        colors.add(new Color(0, 1, 0, 0.5f)); // GREEN
        colors.add(new Color(0, 0, 1, 0.5f)); // BLUE
    });

    static Surface partialOutline(int color, int insetWidth, boolean extendToBounds, OutlineSide ...targetedSides) {
        var sides = Set.of(targetedSides);

        if (OutlineSide.ALL_SIDES.equals(sides)) return outline(color, insetWidth);

        return (context, component) -> {
            for (var side : sides) {
                // Just some janky debug coloring
//                var tempColor = TRANSPARENT_DEBUG_COLORS.get(side.ordinal());

                if (side == OutlineSide.TOP || side == OutlineSide.BOTTOM) {
                    var hasLeftBound = sides.contains(OutlineSide.LEFT) || !extendToBounds;
                    var hasRightBound = sides.contains(OutlineSide.RIGHT) || !extendToBounds;

                    var lineStart = component.x() + (hasLeftBound ? insetWidth : 0);
                    var lineLength = component.width() - ((hasLeftBound ? insetWidth : 0) + (hasRightBound ? insetWidth : 0));

                    var lineY = component.y() + (side == OutlineSide.BOTTOM ? component.height() - insetWidth - 1 : insetWidth);

                    context.drawHorizontalLine(lineStart, lineStart + lineLength - 1, lineY, color);
                } else {
                    var hasTopBound = sides.contains(OutlineSide.TOP) || !extendToBounds;
                    var hasBottomBound = sides.contains(OutlineSide.BOTTOM) || !extendToBounds;

                    var lineStart = component.y() + (hasTopBound ? insetWidth : -1);
                    var lineLength = component.height() - ((hasTopBound ? insetWidth : 0) + (hasBottomBound ? insetWidth : 0));
                    var lineEnd = lineStart + lineLength + (hasBottomBound && hasTopBound ? -1 : (hasBottomBound || hasTopBound) ? 0 : 1);

                    var lineX = component.x() + (side == OutlineSide.RIGHT ? component.width() - insetWidth - 1 : insetWidth);

                    context.drawVerticalLine(lineX, lineStart, lineEnd, color);
                }
            }
        };
    }

    enum OutlineSide {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT;

        public static final Set<OutlineSide> ALL_SIDES = Set.of(OutlineSide.values());
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
                case "outline" -> {
                    var insetWidth = 0;
                    var insetWidthAttr = child.getAttributeNode("insetWidth");

                    if (insetWidthAttr != null) insetWidth = UIParsing.parseUnsignedInt(insetWidthAttr);

                    yield surface.and(outline(Color.parseAndPack(child), insetWidth));
                }
                case "flat" -> surface.and(flat(Color.parseAndPack(child)));
                case "partial-outline" -> {
                    UIParsing.expectAttributes(child, "color");
                    var color = Color.parseAndPack(child.getAttributeNode("color"));

                    var insetWidth = Optional.ofNullable(child.getAttributeNode("insetWidth"))
                            .map(UIParsing::parseUnsignedInt)
                            .orElse(0);

                    var extendToBounds = Optional.ofNullable(child.getAttributeNode("extendToBounds"))
                            .map(UIParsing::parseBool)
                            .orElse(false);

                    var sides = Arrays.stream(child.getFirstChild().getTextContent().split(" "))
                            .map(string -> OutlineSide.valueOf(string.toUpperCase(Locale.ROOT)))
                            .toArray(OutlineSide[]::new);

                    yield surface.and(Surface.partialOutline(color, insetWidth, extendToBounds, sides));
                }
                case "transformed" -> {
                    var innerChildren = UIParsing.childElements(child);

                    var stack = WrappedMatrix2fStack.parseStack(child, innerChildren);

                    UIParsing.expectChildren(child, innerChildren, "surface");

                    var transformedSurface = Surface.parse(innerChildren.get("surface"));

                    yield surface.and((context, component) -> {
                        context.push().applyStackTransformer(stack);

                        transformedSurface.draw(context, component);

                        context.pop();
                    });
                }
                default -> throw new UIModelParsingException("Unknown surface type '" + child.getNodeName() + "'");
            };
        }

        return surface;
    }

}
