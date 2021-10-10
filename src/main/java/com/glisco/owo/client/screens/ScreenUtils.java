package com.glisco.owo.client.screens;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.function.Consumer;

public class ScreenUtils {

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

    public static void insertIntoSlotRange(List<Slot> slots, int beginIndex, int endIndex, ItemStack addition) {
        for (int i = beginIndex; i < endIndex; i++) {
            final var slot = slots.get(i);
            final int countBefore = addition.getCount();

            slot.insertStack(addition);

            if (countBefore != addition.getCount()) slot.markDirty();
            if (addition.getCount() == 0) break;
        }
    }

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

}
