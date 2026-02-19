package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Represents a tab inside an {@link OwoItemGroup} that contains all items in the
 * passed {@code contentTag}. If you want to use {@link OwoItemSettingsExtension#tab(int)} to
 * define the contents, use {@code null} as the tag
 */
public record ItemGroupTab(
    Icon icon,
    Component name,
    ContentSupplier contentSupplier,
    Identifier texture,
    boolean primary
) implements OwoItemGroup.ButtonDefinition {

    public static final Identifier DEFAULT_TEXTURE = Owo.id("textures/gui/tabs.png");

    @Override
    public Component tooltip() {
        return this.name;
    }

    @FunctionalInterface
    public interface ContentSupplier {
        void addItems(CreativeModeTab.ItemDisplayParameters context, CreativeModeTab.Output entries);
    }
}
