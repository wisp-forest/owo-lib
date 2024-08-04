package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

/**
 * A button placed to the right side of the creative inventory. Provides defaults
 * for linking to sites, but can execute arbitrary actions
 */
public final class ItemGroupButton implements OwoItemGroup.ButtonDefinition {

    public static final Identifier ICONS_TEXTURE = Identifier.of("owo", "textures/gui/icons.png");

    private final Icon icon;
    private final Text tooltip;
    private final Identifier texture;
    private final Runnable action;

    public ItemGroupButton(CreativeModeTab group, Icon icon, String name, Identifier texture, Runnable action) {
        this.icon = icon;
        this.tooltip = OwoItemGroup.ButtonDefinition.tooltipFor(group, "button", name);
        this.action = action;
        this.texture = texture;
    }

    public ItemGroupButton(CreativeModeTab group, Icon icon, String name, Runnable action) {
        this(group, icon, name, ItemGroupTab.DEFAULT_TEXTURE, action);
    }

    public static ItemGroupButton github(CreativeModeTab group, String url) {
        return link(group, Icon.of(ICONS_TEXTURE, 0, 0, 64, 64), "github", url);
    }

    public static ItemGroupButton modrinth(CreativeModeTab group, String url) {
        return link(group, Icon.of(ICONS_TEXTURE, 16, 0, 64, 64), "modrinth", url);
    }

    public static ItemGroupButton curseforge(CreativeModeTab group, String url) {
        return link(group, Icon.of(ICONS_TEXTURE, 32, 0, 64, 64), "curseforge", url);
    }

    public static ItemGroupButton discord(CreativeModeTab group, String url) {
        return link(group, Icon.of(ICONS_TEXTURE, 48, 0, 64, 64), "discord", url);
    }

    /**
     * Creates a button that opens the given link when clicked
     *
     * @param icon The icon for this button to use
     * @param name The name of this button, used for the translation key
     * @param url  The url to open
     * @return The created button
     */
    public static ItemGroupButton link(CreativeModeTab group, Icon icon, String name, String url) {
        return new ItemGroupButton(group, icon, name, () -> {
            final var client = Minecraft.getInstance();
            var screen = client.screen;
            client.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) Util.getPlatform().openUri(url);
                client.setScreen(screen);
            }, url, true));
        });
    }

    @Override
    public Identifier texture() {
        return this.texture;
    }

    @Override
    public Icon icon() {
        return this.icon;
    }

    @Override
    public Text tooltip() {
        return this.tooltip;
    }

    public Runnable action() {
        return this.action;
    }

}
