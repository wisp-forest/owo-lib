package io.wispforest.owo.itemgroup.base;

import io.wispforest.owo.itemgroup.core.CondensedEntries;
import io.wispforest.owo.itemgroup.core.CondensedEntry;
import io.wispforest.owo.itemgroup.util.ItemStackOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.SequencedCollection;
import java.util.function.Predicate;

// TODO [ItemGroupPR]: DOCUMENT?
public interface OwoItemGroupEntries extends CondensedEntries.RegistrationCallback {

    default OwoItemGroupEntries addAll(TagKey<? extends ItemConvertible> tagKey) {
        return this.addAll(tagKey, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
    }

    default OwoItemGroupEntries addAll(TagKey<? extends ItemConvertible> tagKey, ItemGroup.StackVisibility visibility) {
        return addAll(ItemStackOps.getStacks(tagKey), visibility);
    }

    default OwoItemGroupEntries add(ItemStack stack) {
        return this.add(stack, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
    }

    default OwoItemGroupEntries add(ItemConvertible item, ItemGroup.StackVisibility visibility) {
        return this.add(new ItemStack(item), visibility);
    }

    default OwoItemGroupEntries add(ItemConvertible item) {
        return this.add(new ItemStack(item), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
    }

    default OwoItemGroupEntries addAll(SequencedCollection<ItemStack> stacks, ItemGroup.StackVisibility visibility) {
        stacks.forEach((stack) -> this.add(stack, visibility));

        return this;
    }

    default OwoItemGroupEntries addAll(SequencedCollection<ItemStack> stacks) {
        this.addAll(stacks, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);

        return this;
    }

    OwoItemGroupEntries add(ItemStack stack, ItemGroup.StackVisibility visibility);

    //--

    @Override
    default OwoItemGroupEntries addEntry(Identifier identifier, Predicate<Item> predicate) {
        return addEntry(identifier, ItemStacksSupplier.of(predicate));
    }

    @Override
    default CondensedEntries.RegistrationCallback addEntry(Identifier identifier, ItemConvertible item) {
        return addEntry(identifier, ItemStacksSupplier.itemVariants(item));
    }

    OwoItemGroupEntries addEntry(TagKey<? extends ItemConvertible> tagKey);

    @Override
    default OwoItemGroupEntries addEntry(Identifier identifier, TagKey<? extends ItemConvertible> tagKey) {
        return addEntry(identifier, ItemStacksSupplier.tag(tagKey));
    }

    @Override
    default OwoItemGroupEntries addEntry(Identifier identifier, SequencedCollection<ItemStack> stacks) {
        return addEntry(identifier, ItemStacksSupplier.stacks(stacks));
    }

    @Override
    default OwoItemGroupEntries addEntry(Identifier identifier, ItemStacksSupplier supplier) {
        return addEntry(new CondensedEntry(identifier, supplier, false));
    }

    OwoItemGroupEntries addEntry(CondensedEntry entry);
}
