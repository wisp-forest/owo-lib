package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.base.ItemStacksSupplier;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupStateImpl;
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

    private static final Map<RegistryKey<ItemGroup>, Map<Integer, Map<Identifier, CondensedEntry>>> ENTRIES = new LinkedHashMap<>();
    private static final Map<RegistryKey<ItemGroup>, Map<Integer, Map<Identifier, CondensedEntry>>> DATA_LOADED_ENTRIES = new LinkedHashMap<>();

    public static void registerEntry(RegistryKey<ItemGroup> targetGroup, int tabIndex, CondensedEntry entry) {
        var entriesMap = ENTRIES.computeIfAbsent(targetGroup, key -> new LinkedHashMap<>())
            .computeIfAbsent(tabIndex, integer -> new LinkedHashMap<>());

        entriesMap.put(entry.id(), entry);
    }

    @ApiStatus.Internal
    public static void registerDataEntries(Map<Identifier, Map<Identifier, Map<Integer, List<CondensedEntry>>>> data) {
        DATA_LOADED_ENTRIES.clear();

        data.forEach((identifier, groupedEntries) -> {
            groupedEntries.forEach((itemGroupId, tabEntries) -> {
                var groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, itemGroupId);

                tabEntries.forEach((tabIndex, entries) -> {
                    CondensedEntries.registerFor(groupKey, tabIndex);

                    var entriesMap = DATA_LOADED_ENTRIES.computeIfAbsent(groupKey, key -> new LinkedHashMap<>())
                        .computeIfAbsent(tabIndex, integer -> new LinkedHashMap<>());

                    entries.forEach(entry -> entriesMap.put(entry.id(), entry));
                });
            });
        });
    }

    public static RegistrationCallback registerFor(RegistryKey<ItemGroup> groupKey) {
        return registerFor(groupKey, 0);
    }

    public static RegistrationCallback registerFor(RegistryKey<ItemGroup> groupKey, int tabIndex) {
        return new RegistrationCallback() {
            @Override
            public RegistrationCallback addEntry(CondensedEntry entry) {
                registerEntry(groupKey, tabIndex, entry);

                return this;
            }
        };
    }

    public static List<Map<Identifier, CondensedEntry>> getEntriesFor(ItemGroup group, IntSet tabIndex) {
        var key = Registries.ITEM_GROUP.getKey(group).orElseThrow();

        var list = new ArrayList<Map<Identifier, CondensedEntry>>();

        // -- Code registered Entries --

        var groupEntries = CondensedEntries.ENTRIES.get(key);

        if (groupEntries != null) {
            list.addAll(tabIndex.intStream().mapToObj(groupEntries::get).filter(Objects::nonNull).toList());
        }

        // -- Data Driven Entries --

        var dataGroupEntries = CondensedEntries.DATA_LOADED_ENTRIES.get(key);

        if (dataGroupEntries != null) {
            list.addAll(tabIndex.intStream().mapToObj(dataGroupEntries::get).filter(Objects::nonNull).toList());
        }

        // -- State Entries --

        var state = OwoItemGroupState.get(group);

        if (state != null) {
            var entries = ((OwoItemGroupStateImpl) state).activeCondensedEntries();

            list.addAll(tabIndex.intStream().mapToObj(entries::get).filter(Objects::nonNull).toList());
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
}


