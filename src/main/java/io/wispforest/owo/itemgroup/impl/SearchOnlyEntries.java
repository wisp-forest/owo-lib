package io.wispforest.owo.itemgroup.impl;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;

public class SearchOnlyEntries extends ItemGroup.EntriesImpl {

    public SearchOnlyEntries(ItemGroup group, FeatureSet enabledFeatures) {
        super(group, enabledFeatures);
    }

    @Override
    public void add(ItemStack stack, ItemGroup.StackVisibility visibility) {
        if (visibility == ItemGroup.StackVisibility.PARENT_TAB_ONLY) return;
        super.add(stack, ItemGroup.StackVisibility.SEARCH_TAB_ONLY);
    }
}
