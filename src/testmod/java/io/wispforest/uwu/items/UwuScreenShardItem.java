package io.wispforest.uwu.items;

import io.wispforest.uwu.EpicMenu;
import io.wispforest.uwu.client.SelectUwuScreenScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class UwuScreenShardItem extends Item {

    public UwuScreenShardItem(Item.Properties settings) {
        super(settings.rarity(Rarity.UNCOMMON));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user.isShiftKeyDown()) {
            if (world.isClientSide()) Minecraft.getInstance().setScreen(new SelectUwuScreenScreen());
        } else if (!world.isClientSide()) {
            user.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("bruh momento");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                    return new EpicMenu(syncId, inv, ContainerLevelAccess.create(world, player.blockPosition()));
                }
            });
        }
        return InteractionResult.PASS;
    }
}
