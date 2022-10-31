package io.wispforest.uwu;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

import java.util.concurrent.ThreadLocalRandom;

public class EpicScreenHandler extends ScreenHandler {

    private final ScreenHandlerContext context;

    public final SyncedProperty<Integer> epicNumber;

    public EpicScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public EpicScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(Uwu.EPIC_SCREEN_HANDLER_TYPE, syncId);
        this.context = context;
        SlotGenerator.begin(this::addSlot, 8, 84)
            .grid(new SimpleInventory(4), 0, 4, 1)
            .playerInventory(inventory);

        this.epicNumber = addProperty(Integer.class, 0);
        this.epicNumber.set(ThreadLocalRandom.current().nextInt());
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        if (!player.world.isClient)
            this.epicNumber.set(ThreadLocalRandom.current().nextInt());

        return ScreenUtils.handleSlotTransfer(this, index, 4);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, Blocks.AIR);
    }
}
