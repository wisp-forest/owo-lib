package io.wispforest.owo.itemgroup.json;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.fabricmc.fabric.mixin.itemgroup.ItemGroupsAccessor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
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

    public WrapperGroup(ItemGroup parent, List<ItemGroupTab> tabs, List<ItemGroupButton> buttons) {
        super(parent.getId(), owoItemGroup -> {}, () -> Icon.of(parent.getIcon()), 4, 4, null, true, false);

        var groups = new ArrayList<>(ItemGroups.getGroups());
        groups.set(groups.indexOf(parent), this);
        ItemGroupsAccessor.setGroups(groups);
        ((FabricItemGroup) this).setPage(((FabricItemGroup) parent).getPage());

        ((ItemGroupAccessor)this).owo$setDisplayName(parent.getDisplayName());
        ((net.fabricmc.fabric.mixin.itemgroup.ItemGroupAccessor)this).setColumn(parent.getColumn());
        ((net.fabricmc.fabric.mixin.itemgroup.ItemGroupAccessor)this).setRow(parent.getRow());

        this.parent = parent;

        this.tabs.addAll(tabs);
        this.buttons.addAll(buttons);
    }

    @Override
    public Identifier getId() {
        return this.parent.getId();
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
