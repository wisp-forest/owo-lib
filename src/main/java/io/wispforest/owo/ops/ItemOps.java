package io.wispforest.owo.ops;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Hand;

/**
 * A collection of common checks and operations done on {@link ItemStack}
 */
public final class ItemOps {

    private ItemOps() {}

    /**
     * Checks if stack one can stack onto stack two
     *
     * @param base     The base stack
     * @param addition The stack to be added
     * @return {@code true} if addition can stack onto base
     */
    public static boolean canStack(ItemStack base, ItemStack addition) {
        return base.isEmpty() || (canIncreaseBy(base, addition.getCount()) && ItemStack.areItemsEqual(base, addition) && ItemStack.areNbtEqual(base, addition));
    }

    /**
     * Checks if a stack can increase
     *
     * @param stack The stack to test
     * @return stack.getCount() &lt; stack.getMaxCount()
     */
    public static boolean canIncrease(ItemStack stack) {
        return stack.isStackable() && stack.getCount() < stack.getMaxCount();
    }

    /**
     * Checks if a stack can increase by the given amount
     *
     * @param stack The stack to test
     * @param by    The amount to test for
     * @return {@code true} if the stack can increase by the given amount
     */
    public static boolean canIncreaseBy(ItemStack stack, int by) {
        return stack.isStackable() && stack.getCount() + by <= stack.getMaxCount();
    }

    /**
     * Returns a copy of the given stack with count set to 1
     */
    public static ItemStack singleCopy(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    /**
     * Decrements the stack
     *
     * @param stack The stack to decrement
     * @param amount The amount to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(ItemStack stack, int amount) {
        stack.decrement(amount);
        return !stack.isEmpty();
    }

    /**
     * Decrements the stack
     *
     * @param stack The stack to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(ItemStack stack) {
        return emptyAwareDecrement(stack, 1);
    }

    /**
     * Decrements the stack in the players hand and replaces it with {@link ItemStack#EMPTY}
     * if the result would be an empty stack
     *
     * @param player The player to operate on
     * @param hand The hand to affect
     * @param amount The amount to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean decrementPlayerHandItem(PlayerEntity player, Hand hand, int amount) {
        var stack = player.getStackInHand(hand);
        if (!player.isCreative()) {
            if (!emptyAwareDecrement(stack, amount)) player.setStackInHand(hand, ItemStack.EMPTY);
        }
        return !stack.isEmpty();
    }

    /**
     * Decrements the stack in the players hand and replaces it with {@link ItemStack#EMPTY}
     * if the result would be an empty stack
     *
     * @param player The player to operate on
     * @param hand   The hand to affect
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean decrementPlayerHandItem(PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!player.isCreative()) {
            if (!emptyAwareDecrement(stack)) player.setStackInHand(hand, ItemStack.EMPTY);
        }
        return !stack.isEmpty();
    }

    /**
     * Stores the given ItemStack with the specified key
     * into the given nbt compound
     *
     * @param stack The stack to store
     * @param nbt   The nbt compound to write to
     * @param key   The key to prefix the stack with
     */
    public static void store(ItemStack stack, NbtCompound nbt, String key) {
        if (stack.isEmpty()) return;

        var stackNbt = new NbtCompound();
        stack.writeNbt(stackNbt);
        nbt.put(key, stackNbt);
    }

    /**
     * Loads the ItemStack stored at the specified key
     * in the given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key to load from
     * @return The deserialized stack
     */
    public static ItemStack get(NbtCompound nbt, String key) {
        if (!nbt.contains(key, NbtElement.COMPOUND_TYPE)) return ItemStack.EMPTY;

        var stackNbt = nbt.getCompound(key);
        return ItemStack.fromNbt(stackNbt);
    }

}