package io.wispforest.uwu;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class EpicScreenHandler extends ScreenHandler {

    private final ScreenHandlerContext context;

    public EpicScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public EpicScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(Uwu.EPIC_SCREEN_HANDLER_TYPE, syncId);
        this.context = context;
        SlotGenerator.begin(this::addSlot, 8, 84).playerInventory(inventory);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, 0);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, Blocks.AIR);
    }
}
