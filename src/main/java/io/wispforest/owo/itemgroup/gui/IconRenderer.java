package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.impl.DefaultOwoItemGroupRenderer;
import net.minecraft.client.gui.DrawContext;

///
/// Used within [DefaultOwoItemGroupRenderer] to render the given icon for a given [OwoItemGroup]
///
public interface IconRenderer<T extends Icon> {
    void renderIcon(T icon, DrawContext context, int x, int y, int mouseX, int mouseY, float partialTicks);
}
