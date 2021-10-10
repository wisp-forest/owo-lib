package com.glisco.owo.client.screens;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.Predicate;

/**
 * A slot that uses the provided {@code insertCondition}
 * to decide which items can be inserted
 */
public class ValidatingSlot extends Slot {

    private final Predicate<ItemStack> insertCondition;

    public ValidatingSlot(Inventory inventory, int index, int x, int y, Predicate<ItemStack> insertCondition) {
        super(inventory, index, x, y);
        this.insertCondition = insertCondition;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return insertCondition.test(stack);
    }

}
