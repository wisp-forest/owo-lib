package io.wispforest.owo.itemgroup.base;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.util.ItemStackOps;
import io.wispforest.owo.serialization.DispatchedEndec;
import io.wispforest.owo.serialization.IdentifiedData;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Predicate;

public interface ItemStacksSupplier {

    @ApiStatus.Internal
    ItemStacksSupplier EMPTY = Collections::emptyList;

    DispatchedEndec<ItemStacksSupplier> ENDEC = DispatchedEndec.ofOptionalIdentifiedData(() -> ItemStacksSupplier.EMPTY)
        .baseClasses(StackCollection.class, RegistryTag.class, ItemVariants.class)
        .allowTypelessData()
        .create();

    SequencedCollection<ItemStack> get();

    static ItemStacksSupplier compound(SequencedCollection<ItemStacksSupplier> suppliers) {
        return new SupplierCollection(suppliers);
    }

    static ItemStacksSupplier stacks(SequencedCollection<net.minecraft.item.ItemStack> stacks) {
        return new StackCollection(stacks);
    }

    static ItemStacksSupplier tag(TagKey<? extends ItemConvertible> tagKey) {
        return new RegistryTag(tagKey);
    }

    static ItemStacksSupplier of(Predicate<net.minecraft.item.Item> predicate) {
        return new ItemPredicate(predicate);
    }

    static ItemStacksSupplier itemVariants(ItemConvertible item) {
        return new ItemVariants(item, false);
    }

    static ItemStacksSupplier itemVariants(ItemConvertible item, boolean overrideClassSupplier) {
        return new ItemVariants(item, overrideClassSupplier);
    }

    static ItemStacksSupplier supplier(ItemStacksSupplier supplier) {
        return (supplier instanceof ItemConvertible convertible)
            ? new ItemVariants(convertible, false)
            : supplier;
    }

    default String translationKey() {
        if (this instanceof IdentifiedData data) {
            return data.getTypeId().toTranslationKey("text.owo.item_stack_supplier.type");
        }

        return this.getClass().getSimpleName();
    }

    record StackCollection(SequencedCollection<net.minecraft.item.ItemStack> stacks) implements ItemStacksSupplier, IdentifiedData {
        public static final Identifier ID = Identifier.of("owo", "itemstacks");

        public static final StructEndec<StackCollection> ENDEC = ItemStacksSupplier.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.ITEM_STACK.listOf().fieldOf("stacks", s -> {
                    return (s.stacks() instanceof List<net.minecraft.item.ItemStack> list) ? list : List.copyOf(s.stacks());
                }),
                StackCollection::new
            ));

        @Override
        public SequencedCollection<net.minecraft.item.ItemStack> get() {
            return stacks;
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    record RegistryTag(TagKey<? extends ItemConvertible> tagKey) implements ItemStacksSupplier, IdentifiedData {

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
        public SequencedCollection<net.minecraft.item.ItemStack> get() {
            return ItemStackOps.getStacks(tagKey());
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    // TODO: CHANGE TO ItemConvertible?
    record ItemPredicate(Predicate<net.minecraft.item.Item> predicate) implements ItemStacksSupplier {
        @Override
        public SequencedCollection<net.minecraft.item.ItemStack> get() {
            return Registries.ITEM.stream()
                .filter(predicate)
                .map(net.minecraft.item.Item::getDefaultStack)
                .toList();
        }

        @Override
        public String translationKey() {
            return "text.owo.item_stack_supplier.type.owo.predicate";
        }
    }

    record ItemVariants(ItemConvertible item, boolean overrideClassSupplier) implements ItemStacksSupplier, IdentifiedData {
        public ItemVariants {
            if (!(item instanceof net.minecraft.item.Item || item instanceof Block)) {
                throw new IllegalStateException("Unable to handle the given registry type for a ItemConvertible ItemStackSupplier: " + item);
            }
        }

        public static final Identifier ID = Identifier.of("owo", "item_variant");

        public static final StructEndec<ItemVariants> ENDEC = ItemStacksSupplier.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.optionalFieldOf("registry", s -> (s.item instanceof Block) ? RegistryKeys.BLOCK.getValue() : RegistryKeys.ITEM.getValue(), RegistryKeys.ITEM.getValue()),
                MinecraftEndecs.IDENTIFIER.fieldOf("entry", s -> (s.item instanceof Block block) ? Registries.BLOCK.getId(block) : Registries.ITEM.getId(s.item.asItem())),
                Endec.BOOLEAN.optionalFieldOf("override_class_supplier", ItemVariants::overrideClassSupplier, false),
                (registry, entryId, overrideClassSupplier) -> new ItemVariants((registry == RegistryKeys.BLOCK.getValue()) ? Registries.BLOCK.get(entryId) : Registries.ITEM.get(entryId), overrideClassSupplier)
            ));

        @Override
        public SequencedCollection<net.minecraft.item.ItemStack> get() {
            if (!overrideClassSupplier && item instanceof ItemStacksSupplier supplier) return supplier.get();

            var itemClazz = item.getClass();

            return Registries.ITEM.stream()
                .filter(itemClazz::isInstance)
                .map(net.minecraft.item.Item::getDefaultStack)
                .toList();
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    record SupplierCollection(SequencedCollection<ItemStacksSupplier> suppliers) implements ItemStacksSupplier, IdentifiedData {
        public static final Identifier ID = Identifier.of("owo", "compound");

        public static final StructEndec<SupplierCollection> ENDEC = ItemStacksSupplier.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                ItemStacksSupplier.ENDEC.listOf().fieldOf("suppliers", s -> {
                    return (s.suppliers() instanceof List<ItemStacksSupplier> list) ? list : List.copyOf(s.suppliers());
                }),
                SupplierCollection::new
            ));

        @Override
        public Identifier getTypeId() {
            return ID;
        }

        @Override
        public SequencedCollection<net.minecraft.item.ItemStack> get() {
            return suppliers.stream().flatMap(supplier -> supplier.get().stream()).toList();
        }
    }
}
