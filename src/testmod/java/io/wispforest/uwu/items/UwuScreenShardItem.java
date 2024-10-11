package io.wispforest.uwu.items;

import io.wispforest.uwu.EpicScreenHandler;
import io.wispforest.uwu.client.SelectUwuScreenScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class UwuScreenShardItem extends Item {

    public UwuScreenShardItem() {
        super(new Settings().rarity(Rarity.UNCOMMON));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            if (world.isClient) MinecraftClient.getInstance().setScreen(new SelectUwuScreenScreen());
        } else if (!world.isClient) {
            user.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.literal("bruh momento");
                }

                @Override
                public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new EpicScreenHandler(syncId, inv, ScreenHandlerContext.create(world, player.getBlockPos()));
                }
            });
        }

        return ActionResult.PASS;
    }
}
