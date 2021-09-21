package com.glisco.owo.itemgroup.gui;

import com.glisco.owo.itemgroup.Icon;
import com.glisco.owo.itemgroup.TabbedItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record ItemGroupButton(Icon icon, String name, Runnable action) implements TabbedItemGroup.DrawableComponent {

    public static final Identifier ICONS_TEXTURE = new Identifier("owo", "textures/gui/icons.png");

    public static ItemGroupButton link(Icon icon, String name, String url) {
        return new ItemGroupButton(icon, name, () -> Util.getOperatingSystem().open(url));
    }

    public static ItemGroupButton github(String url) {
        return link(Icon.of(ICONS_TEXTURE, 0, 0, 64, 64), "github", url);
    }

    public static ItemGroupButton modrinth(String url) {
        return link(Icon.of(ICONS_TEXTURE, 16, 0, 64, 64), "modrinth", url);
    }

    public static ItemGroupButton curseforge(String url) {
        return link(Icon.of(ICONS_TEXTURE, 32, 0, 64, 64), "curseforge", url);
    }

    public static ItemGroupButton discord(String url) {
        return link(Icon.of(ICONS_TEXTURE, 48, 0, 64, 64), "discord", url);
    }

    @Override
    public Identifier texture() {
        return ItemGroupTab.DEFAULT_TEXTURE;
    }

    @Override
    public String getTranslationKey(String groupKey) {
        return "itemGroup." + groupKey + ".button." + name;
    }
}
