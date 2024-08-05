package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.network.chat.Text;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(CreativeModeTab.class)
public interface ItemGroupAccessor {

    @Accessor("displayItemsGenerator")
    CreativeModeTab.DisplayItemsGenerator owo$getEntryCollector();

    @Mutable
    @Accessor("displayItemsGenerator")
    void owo$setEntryCollector(CreativeModeTab.DisplayItemsGenerator collector);

    @Accessor("displayItemsSearchTab")
    void owo$setSearchTabStacks(Set<ItemStack> searchTabStacks);

    @Mutable
    @Accessor("displayName")
    void owo$setDisplayName(Text displayName);

    @Mutable
    @Accessor("column")
    void owo$setColumn(int column);

    @Mutable
    @Accessor("row")
    void owo$setRow(CreativeModeTab.Row row);
}
