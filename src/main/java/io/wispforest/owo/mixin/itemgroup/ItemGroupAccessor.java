package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {

    @Accessor("entryCollector")
    ItemGroup.EntryCollector owo$getEntryCollector();

    @Mutable
    @Accessor("entryCollector")
    void owo$setEntryCollector(ItemGroup.EntryCollector collector);
}
