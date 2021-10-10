package com.glisco.owo.client.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.function.Consumer;

/**
 * A collection of utilities to ease implementing a simple {@link net.minecraft.client.gui.screen.ingame.HandledScreen}
 */
public class ScreenUtils {

    /**
     * Generates the player inventory and hotbar
     * slots seen in most normal inventory screens
     *
     * @param anchorX         The {@code x} coordinate of the top-, leftmost slot
     * @param anchorY         The {@code y} coordinate of the top-, leftmost slot
     * @param playerInventory The inventory to associate the slots with
     * @param slotConsumer    Some method that accepts the generated slots
     */
    public static void generatePlayerSlots(int anchorX, int anchorY, PlayerInventory playerInventory, Consumer<Slot> slotConsumer) {
        int i, j;
        //Player inventory
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                slotConsumer.accept(new Slot(playerInventory, 9 + j + i * 9, anchorX + j * 18, anchorY + i * 18));
            }
        }
        //Player Hotbar
        for (i = 0; i < 9; ++i) {
            slotConsumer.accept(new Slot(playerInventory, i, anchorX + i * 18, anchorY + 58));
        }
    }

    /**
     * Can be used as an implementation of {@link net.minecraft.screen.ScreenHandler#transferSlot(PlayerEntity, int)}
     * for simple screens with a lower (player) and upper (main) inventory
     *
     * <pre>
     * {@code
     * @Override
     * public ItemStack transferSlot(PlayerEntity player, int invSlot) {
     *     return ScreenUtils.handleSlotTransfer(this.slots, invSlot, this.inventory.size());
     * }
     * }
     * </pre>
     *
     * @param slots              The slots of the target ScreenHandler
     * @param clickedSlotIndex   The slot index that was clicked
     * @param upperInventorySize The size of the upper (main) inventory
     * @return The return value for {{@link net.minecraft.screen.ScreenHandler#transferSlot(PlayerEntity, int)}}
     */
    public static ItemStack handleSlotTransfer(List<Slot> slots, int clickedSlotIndex, int upperInventorySize) {
        final var clickedSlot = slots.get(clickedSlotIndex);
        if (!clickedSlot.hasStack()) return ItemStack.EMPTY;

        final var clickedStack = clickedSlot.getStack();

        if (clickedSlotIndex < upperInventorySize) {
            ScreenUtils.insertIntoSlotRange(slots, upperInventorySize, slots.size(), clickedStack);
        } else {
            ScreenUtils.insertIntoSlotRange(slots, 0, upperInventorySize, clickedStack);
        }

        if (clickedStack.isEmpty()) {
            clickedSlot.setStack(ItemStack.EMPTY);
        } else {
            clickedSlot.markDirty();
        }

        return clickedStack;
    }

    /**
     * Tries to insert the {@code addition} stack into all slots in the given range
     *
     * @param slots      The slots to operate on
     * @param beginIndex The index of the first slot to check
     * @param endIndex   The index of the last slot to check
     * @param addition   The ItemStack to try and insert, this gets mutated
     *                   if insertion (partly) succeeds
     */
    public static void insertIntoSlotRange(List<Slot> slots, int beginIndex, int endIndex, ItemStack addition) {
        for (int i = beginIndex; i < endIndex; i++) {
            final var slot = slots.get(i);
            final int countBefore = addition.getCount();

            slot.insertStack(addition);

            if (countBefore != addition.getCount()) slot.markDirty();
            if (addition.getCount() == 0) break;
        }
    }

}
