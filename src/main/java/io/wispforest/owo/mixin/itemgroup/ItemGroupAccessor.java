package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {

    @Accessor("entryCollector")
    ItemGroup.EntryCollector owo$getEntryCollector();

    @Mutable
    @Accessor("entryCollector")
    void owo$setEntryCollector(ItemGroup.EntryCollector collector);

    @Accessor("searchTabStacks")
    void owo$setSearchTabStacks(Set<ItemStack> searchTabStacks);

    @Accessor("searchProviderReloader")
    Consumer<List<ItemStack>> owo$getSearchProviderReloader();

    @Mutable
    @Accessor("displayName")
    void owo$setDisplayName(Text displayName);
}
