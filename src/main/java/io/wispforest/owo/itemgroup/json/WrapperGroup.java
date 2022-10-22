package io.wispforest.owo.itemgroup.json;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import io.wispforest.owo.mixin.itemgroup.FabricItemGroupAccessor;
import net.fabricmc.fabric.mixin.itemgroup.ItemGroupAccessor;
import net.fabricmc.fabric.mixin.itemgroup.ItemGroupsAccessor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;
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

    public WrapperGroup(ItemGroup parent, List<ItemGroupTab> tabs, List<ItemGroupButton> buttons) {
        super(new Identifier("owo", "wrapper"));

        ItemGroups.GROUPS[parent.getIndex()] = this;
        ((ItemGroupAccessor) this).setIndex(parent.getIndex());
        ((io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor) this).owo$setDisplayName(parent.getDisplayName());

        ItemGroupsAccessor.setGroups(ArrayUtils.remove(ItemGroups.GROUPS, ItemGroups.GROUPS.length - 1));
        ((FabricItemGroupAccessor) this).owo$setId(parent.getId());

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

    @Override
    public ItemStackSet getDisplayStacks(FeatureSet enabledFeatures) {
        return this.tabs.size() < 2
                ? parent.getDisplayStacks(enabledFeatures)
                : super.getDisplayStacks(enabledFeatures);
    }

    @Override
    public ItemStackSet getSearchTabStacks(FeatureSet enabledFeatures) {
        return this.tabs.size() < 2
                ? parent.getSearchTabStacks(enabledFeatures)
                : super.getSearchTabStacks(enabledFeatures);
    }

    @Override
    protected void setup() {}

    @Override
    public ItemStack createIcon() {
        return this.parent.createIcon();
    }
}
