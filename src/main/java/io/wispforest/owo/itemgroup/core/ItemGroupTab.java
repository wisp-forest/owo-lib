package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


///
/// Represents a tab inside a given {@link OwoItemGroup} with
/// its entries being gathered in the tabs [OwoEntryCollector].
///
/// When adding the tabs for your [OwoItemGroup] and
/// If you want to use {@link OwoItemSettingsExtension#tab(int)} to
/// define the contents, use {@code null} as the tag
///
public record ItemGroupTab(
        String name,
        Icon icon,
        Text tooltip,
        OwoEntryCollector contentSupplier,
        Identifier texture,
        boolean primary
) implements ButtonDefinition {
    public static final Identifier DEFAULT_TEXTURE = Identifier.of("owo", "textures/gui/tabs.png");
}
