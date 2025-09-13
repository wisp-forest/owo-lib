package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.base.Icon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * A button placed to the right side of the creative inventory. Provides defaults
 * for linking to sites, but can execute arbitrary actions
 */
public class ItemGroupButton implements ButtonDefinition {

    public static final Identifier ICONS_TEXTURE = Identifier.of("owo", "textures/gui/icons.png");

    private final String name;
    private final Icon icon;
    private final Text tooltip;
    private final Identifier texture;
    private final Runnable action;

    public ItemGroupButton(RegistryKey<ItemGroup> groupKey, Icon icon, String name, Identifier texture, Runnable action) {
        this.name = name;
        this.icon = icon;
        this.tooltip = ButtonDefinition.tooltipFor(groupKey, "button", name);
        this.action = action;
        this.texture = texture;
    }

    public ItemGroupButton(RegistryKey<ItemGroup> groupKey, Icon icon, String name, Runnable action) {
        this(groupKey, icon, name, ItemGroupTab.DEFAULT_TEXTURE, action);
    }


    public static Icon iconFromType(String type) {
        return switch (type) {
            case "modrinth" -> Icon.of(ICONS_TEXTURE, 16, 0, 64, 64);
            case "curseforge" -> Icon.of(ICONS_TEXTURE, 32, 0, 64, 64);
            case "github" -> Icon.of(ICONS_TEXTURE, 0, 0, 64, 64);
            case "discord" -> Icon.of(ICONS_TEXTURE, 48, 0, 64, 64);
            default -> Icon.NONE;
        };
    }

    public static ItemGroupButton github(RegistryKey<ItemGroup> groupKey, String url) {
        return link(groupKey, iconFromType("github"), "github", url);
    }

    public static ItemGroupButton modrinth(RegistryKey<ItemGroup> groupKey, String url) {
        return link(groupKey, iconFromType("modrinth"), "modrinth", url);
    }

    public static ItemGroupButton curseforge(RegistryKey<ItemGroup> groupKey, String url) {
        return link(groupKey, iconFromType("curseforge"), "curseforge", url);
    }

    public static ItemGroupButton discord(RegistryKey<ItemGroup> groupKey, String url) {
        return link(groupKey, iconFromType("discord"), "discord", url);
    }

    /**
     * Creates a button that opens the given link when clicked
     *
     * @param icon The icon for this button to use
     * @param name The name of this button, used for the translation key
     * @param url  The url to open
     * @return The created button
     */
    public static ItemGroupButton link(RegistryKey<ItemGroup> groupKey, Icon icon, String name, String url) {
        return new ItemGroupButton(groupKey, icon, name, () -> {
            final var client = MinecraftClient.getInstance();
            var screen = client.currentScreen;
            client.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) Util.getOperatingSystem().open(url);
                client.setScreen(screen);
            }, url, true));
        });
    }

    @Override
    public String name() {
        return this.name;
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
