package io.wispforest.uwu.items;

import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.uwu.client.braid.TestSelector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import java.util.Optional;

public class UwuBraidItem extends Item {

    public UwuBraidItem(Settings settings) {
        super(settings.rarity(Rarity.EPIC));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            var settings = new BraidScreen.Settings();
            settings.shouldPause = false;

            MinecraftClient.getInstance().setScreen(new BraidScreen(settings, new TestSelector()));
        }
        return ActionResult.PASS;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new Tooltip());
    }

    public record Tooltip() implements TooltipData {}
}
