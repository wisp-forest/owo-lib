package io.wispforest.owo.client.screens;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * A slot that uses the provided {@code insertCondition}
 * to decide which items can be inserted
 */
public class ValidatingSlot extends Slot {

    private final Predicate<ItemStack> insertCondition;

    public ValidatingSlot(Container container, int index, int x, int y, Predicate<ItemStack> insertCondition) {
        super(container, index, x, y);
        this.insertCondition = insertCondition;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return insertCondition.test(stack);
    }

}
