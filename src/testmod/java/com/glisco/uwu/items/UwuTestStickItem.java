package com.glisco.uwu.items;

import com.glisco.owo.itemgroup.OwoItemSettings;
import com.glisco.owo.particles.ClientParticles;
import com.glisco.uwu.Uwu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class UwuTestStickItem extends Item {

    public UwuTestStickItem() {
        super(new OwoItemSettings().group(Uwu.SIX_TAB_GROUP).tab(3).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) return TypedActionResult.success(user.getStackInHand(hand));

        ClientParticles.setParticleCount(5);
        ClientParticles.spawnCubeOutline(ParticleTypes.END_ROD, world, user.getEyePos().add(user.getRotationVec(0).multiply(3)).subtract(.5, .5, .5), 1, .01f);

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
