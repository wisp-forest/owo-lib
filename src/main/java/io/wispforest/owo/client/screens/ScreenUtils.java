package io.wispforest.owo.client.screens;

import io.wispforest.owo.mixin.ScreenHandlerInvoker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

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
     * @param handler            The target ScreenHandler
     * @param clickedSlotIndex   The slot index that was clicked
     * @param upperInventorySize The size of the upper (main) inventory
     * @return The return value for {{@link net.minecraft.screen.ScreenHandler#transferSlot(PlayerEntity, int)}}
     */
    public static ItemStack handleSlotTransfer(ScreenHandler handler, int clickedSlotIndex, int upperInventorySize) {
        final var slots = handler.slots;
        final var clickedSlot = slots.get(clickedSlotIndex);
        if (!clickedSlot.hasStack()) return ItemStack.EMPTY;

        final var clickedStack = clickedSlot.getStack();

        if (clickedSlotIndex < upperInventorySize) {
            if (!insertIntoSlotRange(handler, clickedStack, upperInventorySize, slots.size())) return ItemStack.EMPTY;
        } else {
            if (!insertIntoSlotRange(handler, clickedStack, 0, upperInventorySize)) return ItemStack.EMPTY;
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
     * @param handler    The ScreenHandler to operate on
     * @param beginIndex The index of the first slot to check
     * @param endIndex   The index of the last slot to check
     * @param addition   The ItemStack to try and insert, this gets mutated
     *                   if insertion (partly) succeeds
     * @return {@code true} if state was modified
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean insertIntoSlotRange(ScreenHandler handler, ItemStack addition, int beginIndex, int endIndex) {
        return ((ScreenHandlerInvoker) handler).owo$insertItem(addition, beginIndex, endIndex, false);
    }

}
