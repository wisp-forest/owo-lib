package io.wispforest.owo.ops;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A collection of common checks and operations done on {@link ItemStack}
 */
public final class ItemOps {

    private ItemOps() {
    }

    /**
     * Checks if stack one can stack onto stack two
     *
     * @param base     The base stack
     * @param addition The stack to be added
     * @return {@code true} if addition can stack onto base
     */
    public static boolean canStack(ItemStack base, ItemStack addition) {
        return base.isEmpty() || (canIncreaseBy(base, addition.getCount()) && ItemStack.isSameItemSameComponents(base, addition));
    }

    /**
     * Checks if a stack can increase
     *
     * @param stack The stack to test
     * @return stack.getCount() &lt; stack.getMaxCount()
     */
    public static boolean canIncrease(ItemStack stack) {
        return stack.isStackable() && stack.getCount() < stack.getMaxStackSize();
    }

    /**
     * Checks if a stack can increase by the given amount
     *
     * @param stack The stack to test
     * @param by    The amount to test for
     * @return {@code true} if the stack can increase by the given amount
     */
    public static boolean canIncreaseBy(ItemStack stack, int by) {
        return stack.isStackable() && stack.getCount() + by <= stack.getMaxStackSize();
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
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(ItemStack stack) {
        return emptyAwareDecrement(stack, 1);
    }

    /**
     * Decrements the stack
     *
     * @param stack  The stack to decrement
     * @param amount The amount to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(ItemStack stack, int amount) {
        stack.shrink(amount);
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
    public static boolean decrementPlayerHandItem(Player player, InteractionHand hand) {
        return decrementPlayerHandItem(player, hand, 1);
    }

    /**
     * Decrements the stack in the players hand and replaces it with {@link ItemStack#EMPTY}
     * if the result would be an empty stack
     *
     * @param player The player to operate on
     * @param hand   The hand to affect
     * @param amount The amount to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean decrementPlayerHandItem(Player player, InteractionHand hand, int amount) {
        var stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            if (!emptyAwareDecrement(stack, amount)) player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return !stack.isEmpty();
    }
}
