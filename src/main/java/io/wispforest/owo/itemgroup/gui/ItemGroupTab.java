package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Consumer;

/**
 * Represents a tab inside an {@link OwoItemGroup} that contains all items in the
 * passed {@code contentTag}. If you want to use {@link OwoItemSettings#tab(int)} to
 * define the contents, use {@code null} as the tag
 */
public record ItemGroupTab(
        Icon icon,
        Text name,
        Consumer<DefaultedList<ItemStack>> contentSupplier,
        Identifier texture,
        boolean primary
) implements OwoItemGroup.ButtonDefinition {

    public static final Identifier DEFAULT_TEXTURE = new Identifier("owo", "textures/gui/tabs.png");

    @Override
    public String getTranslationKey(String groupKey) {
        return this.name.getContent() instanceof TranslatableTextContent translatable
                ? translatable.getKey()
                : this.name.getString();
    }
}
