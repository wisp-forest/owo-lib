package com.glisco.uwu.items;

import com.glisco.owo.itemgroup.OwoItemSettings;
import com.glisco.owo.ops.WorldOps;
import com.glisco.owo.particles.ClientParticles;
import com.glisco.owo.particles.ServerParticles;
import com.glisco.uwu.Uwu;
import com.glisco.uwu.client.UwuClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UwuTestStickItem extends Item {

    public UwuTestStickItem() {
        super(new OwoItemSettings().group(Uwu.SIX_TAB_GROUP).tab(3).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (user.isSneaking()) {
            if (world.isClient) return TypedActionResult.success(user.getStackInHand(hand));

            var server = user.getServer();
            WorldOps.teleportToWorld((ServerPlayerEntity) user, server.getWorld(World.END), new Vec3d(0, 128, 0));

            return TypedActionResult.success(user.getStackInHand(hand));
        } else {
            if (!world.isClient) return TypedActionResult.success(user.getStackInHand(hand));

            ClientParticles.setParticleCount(5);
            ClientParticles.spawnCubeOutline(ParticleTypes.END_ROD, world, user.getEyePos().add(user.getRotationVec(0).multiply(3)).subtract(.5, .5, .5), 1, .01f);

            return TypedActionResult.success(user.getStackInHand(hand));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getPlayer().isSneaking()) return ActionResult.PASS;
        if (context.getWorld().isClient) return ActionResult.SUCCESS;

        final var breakStack = new ItemStack(Items.NETHERITE_PICKAXE);
        breakStack.addEnchantment(Enchantments.FORTUNE, 3);
        WorldOps.breakBlockWithItem(context.getWorld(), context.getBlockPos(), breakStack);

        ServerParticles.issueEvent((ServerWorld) context.getWorld(), Vec3d.of(context.getBlockPos()), UwuClient.BREAK_BLOCK_PARTICLES);

        return ActionResult.SUCCESS;
    }
}
