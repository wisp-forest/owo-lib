package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents a tab inside an {@link OwoItemGroup} that contains all items in the
 * passed {@code contentTag}. If you want to use {@link OwoItemSettings#tab(int)} to
 * define the contents, use {@code null} as the tag
 */
public record ItemGroupTab(
        Icon icon,
        Text name,
        ContentSupplier contentSupplier,
        Identifier texture,
        boolean primary
) implements OwoItemGroup.ButtonDefinition {

    public static final Identifier DEFAULT_TEXTURE = new Identifier("owo", "textures/gui/tabs.png");

    @Override
    public Text tooltip() {
        return this.name;
    }

    @FunctionalInterface
    public interface ContentSupplier {
        void addItems(FeatureSet enabledFeatures, ItemGroup.Entries entries, boolean hasPermissions);
    }
}
