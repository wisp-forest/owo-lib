package com.glisco.owo;

import net.minecraft.item.ItemStack;

public class ItemOps {

    private ItemOps() {}

    /**
     * Checks if stack two can stack onto stack one
     *
     * @param one The base stack
     * @param two The stack to be added
     * @return Whether that operation is legal
     */
    public static boolean canStack(ItemStack one, ItemStack two) {
        return canIncrease(one) && ItemStack.areItemsEqual(one, two) && ItemStack.areTagsEqual(one, two);
    }

    /**
     * Checks if a stack can increase
     *
     * @param stack The stack to test
     * @return stack.getCount() < stack.getMaxCount()
     */
    public static boolean canIncrease(ItemStack stack) {
        return stack.getCount() < stack.getMaxCount();
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
     * @return false if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(ItemStack stack) {
        stack.decrement(1);
        return !stack.isEmpty();
    }

}
