package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents a tab inside an {@link OwoItemGroup} that contains all items in the
 * passed {@code contentTag}. If you want to use {@link OwoItemSettingsExtension#tab(int)} to
 * define the contents, use {@code null} as the tag
 */
public record ItemGroupTab(
        String name,
        Icon icon,
        Text tooltip,
        ContentSupplier contentSupplier,
        Identifier texture,
        boolean primary
) implements ButtonDefinition {
    public static final Identifier DEFAULT_TEXTURE = Identifier.of("owo", "textures/gui/tabs.png");
}
