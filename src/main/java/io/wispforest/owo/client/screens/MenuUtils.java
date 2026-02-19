package io.wispforest.owo.client.screens;

import io.wispforest.owo.mixin.AbstractContainerMenuInvoker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * A collection of utilities to ease implementing a simple {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}
 */
public class MenuUtils {

    /**
     * Can be used as an implementation of {@link net.minecraft.world.inventory.AbstractContainerMenu#quickMoveStack(Player, int)}
     * for simple screens with a lower (player) and upper (main) inventory
     *
     * <pre>
     * {@code
     * @Override
     * public ItemStack quickMove(PlayerEntity player, int invSlot) {
     *     return MenuUtils.handleSlotTransfer(this, invSlot, this.inventory.size());
     * }
     * }
     * </pre>
     *
     * @param menu               The target AbstractContainerMenu
     * @param clickedSlotIndex   The slot index that was clicked
     * @param upperInventorySize The size of the upper (main) inventory
     * @return The return value for {{@link net.minecraft.world.inventory.AbstractContainerMenu#quickMoveStack(Player, int)}}
     */
    public static ItemStack handleSlotTransfer(AbstractContainerMenu menu, int clickedSlotIndex, int upperInventorySize) {
        final var slots = menu.slots;
        final var clickedSlot = slots.get(clickedSlotIndex);
        if (!clickedSlot.hasItem()) return ItemStack.EMPTY;

        final var clickedStack = clickedSlot.getItem();

        if (clickedSlotIndex < upperInventorySize) {
            if (!insertIntoSlotRange(menu, clickedStack, upperInventorySize, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!insertIntoSlotRange(menu, clickedStack, 0, upperInventorySize)) {
                return ItemStack.EMPTY;
            }
        }

        if (clickedStack.isEmpty()) {
            clickedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }

        return clickedStack;
    }

    /**
     * Shorthand of {@link #insertIntoSlotRange(AbstractContainerMenu, ItemStack, int, int, boolean)} with
     * {@code false} for {@code fromLast}
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean insertIntoSlotRange(AbstractContainerMenu menu, ItemStack addition, int beginIndex, int endIndex) {
        return insertIntoSlotRange(menu, addition, beginIndex, endIndex, false);
    }

    /**
     * Tries to insert the {@code addition} stack into all slots in the given range
     *
     * @param menu       The AbstractContainerMenu to operate on
     * @param beginIndex The index of the first slot to check
     * @param endIndex   The index of the last slot to check
     * @param addition   The ItemStack to try and insert, this gets mutated
     *                   if insertion (partly) succeeds
     * @param fromLast   If {@code true}, iterate the range of slots in
     *                   opposite order
     * @return {@code true} if state was modified
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean insertIntoSlotRange(AbstractContainerMenu menu, ItemStack addition, int beginIndex, int endIndex, boolean fromLast) {
        return ((AbstractContainerMenuInvoker) menu).owo$insertItem(addition, beginIndex, endIndex, fromLast);
    }

}
