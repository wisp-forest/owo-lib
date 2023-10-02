package io.wispforest.owo.itemgroup.json;

import com.mojang.serialization.Lifecycle;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.mixin.ui.SimpleRegistryAccessor;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

/**
 * Used to replace a vanilla or modded item group to add the JSON-defined
 * tabs while keeping the same name, id and icon
 */
@ApiStatus.Internal
public class WrapperGroup extends OwoItemGroup {

    private final ItemGroup parent;
    private boolean extension = false;

    @SuppressWarnings("unchecked")
    public WrapperGroup(ItemGroup parent, Identifier parentId, List<ItemGroupTab> tabs, List<ItemGroupButton> buttons) {
        super(parentId, owoItemGroup -> {}, () -> Icon.of(parent.getIcon()), 4, 4, null, null, null, true, false, false);

        int parentRawId = Registries.ITEM_GROUP.getRawId(parent);

        ((SimpleRegistryAccessor<ItemGroup>) Registries.ITEM_GROUP).owo$getValueToEntry().remove(parent);
        ((SimpleRegistryAccessor<ItemGroup>) Registries.ITEM_GROUP).owo$getEntryToLifecycle().remove(parent);
        ((SimpleRegistry<ItemGroup>) Registries.ITEM_GROUP).set(parentRawId, RegistryKey.of(RegistryKeys.ITEM_GROUP, parentId), this, Lifecycle.stable());

        ((ItemGroupAccessor) this).owo$setDisplayName(parent.getDisplayName());
        ((ItemGroupAccessor) this).owo$setColumn(parent.getColumn());
        ((ItemGroupAccessor) this).owo$setRow(parent.getRow());

        this.parent = parent;

        this.tabs.addAll(tabs);
        this.buttons.addAll(buttons);
    }

    public void addTabs(Collection<ItemGroupTab> tabs) {
        this.tabs.addAll(tabs);
    }

    public void addButtons(Collection<ItemGroupButton> buttons) {
        this.buttons.addAll(buttons);
    }

    public void markExtension() {
        if (this.extension) return;
        this.extension = true;

        if (this.tabs.get(0) == PLACEHOLDER_TAB) {
            this.tabs.remove(0);
        }

        this.tabs.add(0, new ItemGroupTab(
                Icon.of(this.parent.getIcon()),
                this.parent.getDisplayName(),
                ((ItemGroupAccessor) this.parent).owo$getEntryCollector()::accept,
                ItemGroupTab.DEFAULT_TEXTURE,
                true
        ));
    }
}
