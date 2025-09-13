package io.wispforest.owo.itemgroup.base;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.util.ItemStackUtils;
import io.wispforest.owo.serialization.DispatchedEndec;
import io.wispforest.owo.serialization.IdentifiedData;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ItemStacksSupplier extends Supplier<SequencedCollection<ItemStack>> {

    DispatchedEndec<ItemStacksSupplier> ENDEC = DispatchedEndec.<ItemStacksSupplier>of(IdentifiedData.optionalIDGetter())
        .loadClasses(StackCollection.class, RegistryTag.class, ItemClass.class);

    static ItemStacksSupplier of(SequencedCollection<ItemStack> stacks) {
        return new StackCollection(stacks);
    }

    static ItemStacksSupplier of(TagKey<? extends ItemConvertible> tagKey) {
        return new RegistryTag(tagKey);
    }

    static ItemStacksSupplier of(Predicate<Item> predicate) {
        return new ItemPredicate(predicate);
    }

    static ItemStacksSupplier of(ItemConvertible item) {
        return new ItemClass(item, false);
    }

    static ItemStacksSupplier of(ItemConvertible item, boolean overrideClassSupplier) {
        return new ItemClass(item, overrideClassSupplier);
    }

    static ItemStacksSupplier of(ItemStacksSupplier supplier) {
        return (supplier instanceof ItemConvertible convertible)
            ? new ItemClass(convertible, false)
            : supplier;
    }

    record StackCollection(SequencedCollection<ItemStack> stacks) implements ItemStacksSupplier {
        public static final Identifier ID = Identifier.of("owo", "itemstacks");

        public static final StructEndec<StackCollection> ENDEC = ItemStacksSupplier.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.ITEM_STACK.listOf().fieldOf("stacks", s -> {
                    return (s.stacks() instanceof List<ItemStack> list) ? list : List.copyOf(s.stacks());
                }),
                StackCollection::new
            ));

        @Override
        public SequencedCollection<ItemStack> get() {
            return stacks;
        }
    }

    record RegistryTag(TagKey<? extends ItemConvertible> tagKey) implements ItemStacksSupplier {

        public RegistryTag {
            var registryKey = tagKey.registryRef();

            if (!(registryKey == RegistryKeys.ITEM || registryKey == RegistryKeys.BLOCK)) {
                throw new IllegalStateException("Unable to handle the given registry type for a Tag ItemStackSupplier: " + registryKey.getValue());
            }
        }

        public static final Identifier ID = Identifier.of("owo", "tag");

        public static final StructEndec<RegistryTag> ENDEC = ItemStacksSupplier.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.optionalFieldOf("registry", s -> s.tagKey().registryRef().getValue(), RegistryKeys.ITEM.getValue()),
                MinecraftEndecs.IDENTIFIER.fieldOf("tag", s -> s.tagKey().id()),
                (registry, tagId) -> {
                    var key = RegistryKey.ofRegistry(registry);

                    return new RegistryTag((TagKey<? extends ItemConvertible>) (Object) TagKey.of(key, tagId));
                }
            ));

        @Override
        public SequencedCollection<ItemStack> get() {
            return ItemStackUtils.getStacks(tagKey());
        }
    }

    // TODO: CHANGE TO ItemConvertible?
    record ItemPredicate(Predicate<Item> predicate) implements ItemStacksSupplier {
        @Override
        public SequencedCollection<ItemStack> get() {
            return Registries.ITEM.stream()
                .filter(predicate)
                .map(Item::getDefaultStack)
                .toList();
        }
    }

    record ItemClass(ItemConvertible item, boolean overrideClassSupplier) implements ItemStacksSupplier {
        public ItemClass {
            if (!(item instanceof Item || item instanceof Block)) {
                throw new IllegalStateException("Unable to handle the given registry type for a ItemConvertible ItemStackSupplier: " + item);
            }
        }

        public static final Identifier ID = Identifier.of("owo", "item");

        public static final StructEndec<ItemClass> ENDEC = ItemStacksSupplier.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.optionalFieldOf("registry", s -> (s.item instanceof Block) ? RegistryKeys.BLOCK.getValue() : RegistryKeys.ITEM.getValue(), RegistryKeys.ITEM.getValue()),
                MinecraftEndecs.IDENTIFIER.fieldOf("value", s -> (s.item instanceof Block block) ? Registries.BLOCK.getId(block) : Registries.ITEM.getId(s.item.asItem())),
                Endec.BOOLEAN.optionalFieldOf("override_class_supplier", ItemClass::overrideClassSupplier, false),
                (registry, entryId, overrideClassSupplier) -> new ItemClass((registry == RegistryKeys.BLOCK.getValue()) ? Registries.BLOCK.get(entryId) : Registries.ITEM.get(entryId), overrideClassSupplier)
            ));

        @Override
        public SequencedCollection<ItemStack> get() {
            if (!overrideClassSupplier && item instanceof ItemStacksSupplier supplier) return supplier.get();

            var itemClazz = item.getClass();

            return Registries.ITEM.stream()
                .filter(itemClazz::isInstance)
                .map(Item::getDefaultStack)
                .toList();
        }
    }
}
