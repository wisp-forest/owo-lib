package io.wispforest.owo.ui.definitions;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.Drawer;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Surface {

    Surface PANEL = (matrices, component) -> {
        Drawer.drawPanel(matrices, component.x(), component.y(), component.width(), component.height(), false);
    };

    Surface DARK_PANEL = (matrices, component) -> {
        Drawer.drawPanel(matrices, component.x(), component.y(), component.width(), component.height(), true);
    };

    Surface OPTIONS_BACKGROUND = (matrices, component) -> {
        RenderSystem.setShaderTexture(0, Drawer.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(64 / 255f, 64 / 255f, 64 / 255f, 1);
        Drawer.drawTexture(matrices, component.x(), component.y(), 0, 0, component.width(), component.height(), 32, 32);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    };

    Surface BLANK = (matrices, component) -> {};

    static Surface flat(int color) {
        return (matrices, component) -> Drawer.fill(matrices, component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
    }

    static Surface outline(int color) {
        return (matrices, component) -> Drawer.drawRectOutline(matrices, component.x(), component.y(), component.width(), component.height(), color);
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
                case "options-background" -> surface.and(OPTIONS_BACKGROUND);
                case "outline" -> surface.and(outline(UIParsing.parseColor(child)));
                case "flat" -> surface.and(flat(UIParsing.parseColor(child)));
                default -> throw new UIModelParsingException("Unknown surface type '" + child.getNodeName() + "'");
            };
        }

        return surface;
    }
}
