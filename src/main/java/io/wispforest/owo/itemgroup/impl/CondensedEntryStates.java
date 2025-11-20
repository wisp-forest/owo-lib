package io.wispforest.owo.itemgroup.impl;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.core.CondensedEntries;
import io.wispforest.owo.itemgroup.core.CondensedEntry;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

///
/// Handles current state of all condensed entries when inside the [CreativeInventoryScreen] after
/// locating them via [CondensedEntries#getEntriesFor]
///
@ApiStatus.Internal
public class CondensedEntryStates {

    private static final Map<ItemStack, CondensedEntry.State> PARENT_TO_STATE = new IdentityHashMap<>();
    private static final Map<ItemStack, CondensedEntry.State> CHILD_TO_STATE = new IdentityHashMap<>();

    @Nullable
    public static Results getState(ItemStack stack) {
        var state = PARENT_TO_STATE.get(stack);

        if (state != null) return new Results(true, state);

        state = CHILD_TO_STATE.get(stack);

        if (state != null) return new Results(false, state);

        return null;
    }

    public static void forAllState(Consumer<CondensedEntry.State> consumer) {
        PARENT_TO_STATE.values().forEach(consumer);
    }

    public record Results(boolean isParent, CondensedEntry.State state) { }

    public static boolean handleCondensedEntries(ItemGroup.DisplayContext ctx, ItemGroup group, IntSet activeTabIndices, List<ItemStack> displayStacks) {
        PARENT_TO_STATE.clear();
        CHILD_TO_STATE.clear();

        if (Owo.CONFIG.expandedCondensedEntries()) return false;

        var entriesWereAdded = false;

        for (var entry : CondensedEntries.getEntriesFor(ctx, group, activeTabIndices)) {
            final var children = entry.childrenEntries().get()
                .stream()
                .sorted((o1, o2) -> {
                    var indexO1 = getIndex(o1, displayStacks);
                    var indexO2 = getIndex(o2, displayStacks);

                    return Integer.compare(indexO1, indexO2);
                })
                .map(ItemStack::copy)
                .toList();

            // TODO: MAYBE POSSIBLY ADD ERROR ABOUT THE FACT ITS EMPTY IN DEV SIMILAR TO OTHER METHODS?
            if (children.isEmpty()) continue;
            if (children.size() == 1) continue;

            entriesWereAdded = true;

            final var parent = children.getFirst().copy();

            var parentIndex = getIndex(parent, displayStacks);

            if (parentIndex == -1) {
                displayStacks.add(parent);
            } else {
                displayStacks.add(parentIndex, parent);

                displayStacks.remove(parentIndex + 1);
            }

            displayStacks.removeIf(stack -> {
                if (stack == parent || PARENT_TO_STATE.containsKey(stack)) return false;

                return children.stream()
                    .anyMatch(child -> {
                        return ItemStack.areItemsAndComponentsEqual(stack, child)
                            || (entry.useItemMatching() && ItemStack.areItemsEqual(stack, child));
                    });
            });

            final var state = entry.createState(parent, children);

            PARENT_TO_STATE.put(parent, state);

            for (var child : children) {
                CHILD_TO_STATE.put(child, state);
            }
        }

        return entriesWereAdded;
    }

    private static int getIndex(ItemStack stack, List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            var otherStack = stacks.get(i);

            if (ItemStack.areItemsAndComponentsEqual(stack, otherStack) && !PARENT_TO_STATE.containsKey(otherStack)) {
                return i;
            }
        }

        return -1;
    }
}
