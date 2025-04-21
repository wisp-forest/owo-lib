package io.wispforest.uwu.items;

import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.uwu.EpicScreenHandler;
import io.wispforest.uwu.client.SelectUwuScreenScreen;
import io.wispforest.uwu.client.braid.TestSelector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class UwuBraidItem extends Item {

    public UwuBraidItem(Settings settings) {
        super(settings.rarity(Rarity.EPIC));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) MinecraftClient.getInstance().setScreen(new BraidScreen(new TestSelector()));
        return ActionResult.PASS;
    }
}
