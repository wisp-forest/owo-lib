package io.wispforest.owo.itemgroup.impl;

import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.core.*;
import io.wispforest.owo.itemgroup.gui.ScrollerTextures;
import io.wispforest.owo.itemgroup.gui.TabTextures;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class OwoItemGroupImpl implements OwoItemGroup {

    public final List<ItemGroupTab> tabs = new ArrayList<>();
    public final List<ItemGroupButton> buttons = new ArrayList<>();

    private final RegistryKey<ItemGroup> itemGroupId;

    private final Supplier<Icon> iconSupplier;
    private Icon icon = null;

    private @Nullable Identifier backgroundTexture;
    private final @Nullable ScrollerTextures scrollerTextures;
    private final @Nullable TabTextures tabTextures;

    private final int tabStackHeight;
    private final int buttonStackHeight;
    private final boolean useDynamicTitle;
    private final boolean allowMultiSelect;

    public OwoItemGroupImpl(RegistryKey<ItemGroup> itemGroupId, Supplier<Icon> iconSupplier,
                            @Nullable Identifier backgroundTexture, @Nullable ScrollerTextures scrollerTextures, @Nullable TabTextures tabTextures,
                            int tabStackHeight, int buttonStackHeight, boolean useDynamicTitle, boolean allowMultiSelect
    ) {
        this.itemGroupId = itemGroupId;

        this.iconSupplier = iconSupplier;
        this.backgroundTexture = backgroundTexture;
        this.scrollerTextures = scrollerTextures;
        this.tabTextures = tabTextures;

        this.tabStackHeight = tabStackHeight;
        this.buttonStackHeight = buttonStackHeight;
        this.useDynamicTitle = useDynamicTitle;
        this.allowMultiSelect = allowMultiSelect;
    }

    public static ItemGroup.EntryCollector createCollector(ItemGroup group) {
        return (context, entries) -> {
            var state = OwoItemGroupState.getState(group, context);

            if (state != null) state.accept(context, entries);
        };
    }

    //--

    public void addButton(ItemGroupButton button) {
        this.buttons.add(button);
    }

    public void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, Identifier texture, boolean primary) {
        this.tabs.add(new ItemGroupTab(
            name,
            icon,
            ButtonDefinition.tooltipFor(this.itemGroupId(), "tab", name),
            contentTag == null ? ContentSupplier.EMPTY : ContentSupplier.fromTag(contentTag),
            texture,
            primary
        ));
    }

    public void addTab(Icon icon, String name, @Nullable TagKey<Item> contentTag, boolean primary) {
        addTab(icon, name, contentTag, ItemGroupTab.DEFAULT_TEXTURE, primary);
    }

    public void addCustomTab(Icon icon, String name, ContentSupplier contentSupplier, Identifier texture, boolean primary) {
        this.tabs.add(new ItemGroupTab(
            name,
            icon,
            ButtonDefinition.tooltipFor(this.itemGroupId(), "tab", name),
            contentSupplier,
            texture,
            primary
        ));
    }

    public void addCustomTab(Icon icon, String name, ContentSupplier contentSupplier, boolean primary) {
        this.addCustomTab(icon, name, contentSupplier, ItemGroupTab.DEFAULT_TEXTURE, primary);
    }

    public void addTabs(Collection<ItemGroupTab> tabs) {
        this.tabs.addAll(tabs);
    }

    public void addButtons(Collection<ItemGroupButton> buttons) {
        this.buttons.addAll(buttons);
    }

    public void addTabFirst(ItemGroupTab tab) {
        this.tabs.addFirst(tab);
    }

    public boolean hasTab(String name) {
        for (var tab : this.tabs) {
            if (tab.name().equals(name)) {
                return true;
            }
        }

        return false;
    }

    //--

    @Override
    @Nullable
    public Identifier backgroundTexture() {
        return backgroundTexture;
    }

    @Override
    public @Nullable ScrollerTextures scrollerTextures() {
        return scrollerTextures;
    }

    @Override
    public @Nullable TabTextures tabTextures() {
        return tabTextures;
    }

    @Override
    public int tabStackHeight() {
        return tabStackHeight;
    }

    @Override
    public int buttonStackHeight() {
        return buttonStackHeight;
    }

    @Override
    public boolean useDynamicTitle() {
        return useDynamicTitle;
    }

    @Override
    public boolean allowMultiSelect() {
        return allowMultiSelect;
    }

    @Override
    public Icon icon() {
        if (icon == null) icon = iconSupplier.get();

        return icon;
    }

    @Override
    public List<ItemGroupTab> getTabs() {
        return this.tabs;
    }

    @Override
    public List<ItemGroupButton> getButtons() {
        return this.buttons;
    }

    @Override
    public RegistryKey<ItemGroup> itemGroupId() {
        return this.itemGroupId;
    }
}
