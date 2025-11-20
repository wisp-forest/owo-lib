package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.base.ItemStacksSupplier;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Predicate;

// TODO [ItemGroupPR]: DOCUMENT
public class CondensedEntries {

    private static final Map<Identifier, CondensedEntry> ENTRIES = new LinkedHashMap<>();
    private static final Map<Identifier, CondensedEntry> DATA_LOADED_ENTRIES = new LinkedHashMap<>();

    private static final Map<RegistryKey<ItemGroup>, Map<Integer, Set<Identifier>>> ITEM_GROUP_PATH_TO_REGISTERED_ENTRIES = new LinkedHashMap<>();
    private static final Map<RegistryKey<ItemGroup>, Map<Integer, Set<Identifier>>> ITEM_GROUP_PATH_TO_DATA_LOADED_ENTRIES = new LinkedHashMap<>();

    public static void registerEntry(RegistryKey<ItemGroup> targetGroup, int tabIndex, CondensedEntry entry) {
        registerEntry(entry);

        bindEntryToGroupPath(targetGroup, tabIndex, entry.id());
    }

    public static void registerEntry(CondensedEntry entry) {
        if (ENTRIES.containsKey(entry.id())) {
            throw new IllegalStateException("Unable to register entry as an entry with the given Identifier already exists: " + entry.id());
        }

        ENTRIES.putIfAbsent(entry.id(), entry);
    }

    public static void bindEntryToGroupPath(RegistryKey<ItemGroup> targetGroup, int tabIndex, Identifier entryId) {
        var entriesMap = ITEM_GROUP_PATH_TO_REGISTERED_ENTRIES.computeIfAbsent(targetGroup, key -> new LinkedHashMap<>())
            .computeIfAbsent(tabIndex, integer -> new LinkedHashSet<>());

        entriesMap.add(entryId);
    }

    public static CondensedEntry getEntry(Identifier id) {
        if (ENTRIES.containsKey(id)) return ENTRIES.get(id);
        if (DATA_LOADED_ENTRIES.containsKey(id)) return DATA_LOADED_ENTRIES.get(id);
        return null;
    }

    @ApiStatus.Internal
    public static void registerDataEntries(Map<Identifier, Map<Identifier, Map<Integer, List<CondensedEntry>>>> data) {
        ITEM_GROUP_PATH_TO_DATA_LOADED_ENTRIES.clear();
        DATA_LOADED_ENTRIES.clear();

        data.forEach((identifier, groupedEntries) -> {
            groupedEntries.forEach((itemGroupId, tabEntries) -> {
                var groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, itemGroupId);

                tabEntries.forEach((tabIndex, entries) -> {
                    var entriesMap = ITEM_GROUP_PATH_TO_DATA_LOADED_ENTRIES.computeIfAbsent(groupKey, key -> new LinkedHashMap<>())
                        .computeIfAbsent(tabIndex, integer -> new LinkedHashSet<>());

                    entries.forEach(entry -> {
                        entriesMap.add(entry.id());

                        DATA_LOADED_ENTRIES.putIfAbsent(entry.id(), entry);
                    });
                });
            });
        });
    }

    public static RegistrationCallback register() {
        return new RegistrationCallback() {
            @Override
            public RegistrationCallback addEntry(CondensedEntry entry) {
                registerEntry(entry);

                return this;
            }
        };
    }


    public static ItemGroupRegistrationCallback registerFor(RegistryKey<ItemGroup> groupKey) {
        return registerFor(groupKey, 0);
    }

    public static ItemGroupRegistrationCallback registerFor(RegistryKey<ItemGroup> groupKey, int tabIndex) {
        return new ItemGroupRegistrationCallback() {
            @Override
            public ItemGroupRegistrationCallback addEntry(CondensedEntry entry) {
                registerEntry(groupKey, tabIndex, entry);

                return this;
            }

            @Override
            public ItemGroupRegistrationCallback addEntryReference(Identifier entryId) {
                bindEntryToGroupPath(groupKey, tabIndex, entryId);

                return this;
            }
        };
    }

    public static List<CondensedEntry> getEntriesFor(ItemGroup.DisplayContext ctx, ItemGroup group, IntSet tabIndex) {
        var key = Registries.ITEM_GROUP.getKey(group).orElseThrow();

        var list = new ArrayList<CondensedEntry>();

        // -- Code registered Entries --

        var groupEntries = CondensedEntries.ITEM_GROUP_PATH_TO_REGISTERED_ENTRIES.get(key);

        if (groupEntries != null) {
            // TODO: WARN ABOUT POSSIBLE MISSING ENTRIES IN PROD AND CRASH IN DEV
            list.addAll(
                tabIndex.intStream()
                    .mapToObj(groupEntries::get)
                    .filter(Objects::nonNull)
                    .flatMap(ids -> ids.stream().map(ENTRIES::get))
                    .toList()
            );
        }

        // -- Data Driven Entries --

        var dataGroupEntries = CondensedEntries.ITEM_GROUP_PATH_TO_DATA_LOADED_ENTRIES.get(key);

        if (dataGroupEntries != null) {
            // TODO: WARN ABOUT POSSIBLE MISSING ENTRIES IN PROD AND CRASH IN DEV
            list.addAll(
                tabIndex.intStream()
                    .mapToObj(dataGroupEntries::get)
                    .filter(Objects::nonNull)
                    .flatMap(ids -> ids.stream().map(DATA_LOADED_ENTRIES::get))
                    .toList()
            );
        }

        // -- State Entries --

        var state = OwoItemGroupState.get(group);

        if (state != null) {
            list.addAll(state.gatherGlobalCondensedEntries(ctx, tabIndex).values());
        }

        //--

        return list;
    }

    public interface RegistrationCallback {
        default RegistrationCallback addEntry(Identifier identifier, Predicate<Item> predicate) {
            return addEntry(identifier, ItemStacksSupplier.of(predicate));
        }

        default RegistrationCallback addEntry(Identifier identifier, TagKey<? extends ItemConvertible> tagKey) {
            return addEntry(identifier, ItemStacksSupplier.tag(tagKey));
        }

        default RegistrationCallback addEntry(Identifier identifier, ItemConvertible item) {
            return addEntry(identifier, ItemStacksSupplier.itemVariants(item));
        }

//        default RegistrationCallback addItems(Identifier identifier, SequencedCollection<? extends ItemConvertible> items) {
//            return addEntry(identifier, ItemStacksSupplier.of(ItemStackUtils.convertItems(items)));
//        }

        default RegistrationCallback addEntry(Identifier identifier, SequencedCollection<ItemStack> stacks) {
            return addEntry(identifier, ItemStacksSupplier.stacks(stacks));
        }

        default RegistrationCallback addEntry(Identifier identifier, ItemStacksSupplier supplier) {
            return addEntry(new CondensedEntry(identifier, supplier, false));
        }

        RegistrationCallback addEntry(CondensedEntry entry);
    }

    public interface ItemGroupRegistrationCallback extends RegistrationCallback {
        ItemGroupRegistrationCallback addEntryReference(Identifier entryId);

        //--

        @Override
        default ItemGroupRegistrationCallback addEntry(Identifier identifier, Predicate<Item> predicate) {
            RegistrationCallback.super.addEntry(identifier, predicate);

            return this;
        }

        @Override
        default ItemGroupRegistrationCallback addEntry(Identifier identifier, TagKey<? extends ItemConvertible> tagKey) {
            RegistrationCallback.super.addEntry(identifier, tagKey);

            return this;
        }

        @Override
        default ItemGroupRegistrationCallback addEntry(Identifier identifier, ItemConvertible item) {
            RegistrationCallback.super.addEntry(identifier, item);

            return this;
        }

        @Override
        default ItemGroupRegistrationCallback addEntry(Identifier identifier, SequencedCollection<ItemStack> stacks) {
            RegistrationCallback.super.addEntry(identifier, stacks);

            return this;
        }

        @Override
        default ItemGroupRegistrationCallback addEntry(Identifier identifier, ItemStacksSupplier supplier) {
            RegistrationCallback.super.addEntry(identifier, supplier);

            return this;
        }

        @Override
        ItemGroupRegistrationCallback addEntry(CondensedEntry entry);
    }
}


