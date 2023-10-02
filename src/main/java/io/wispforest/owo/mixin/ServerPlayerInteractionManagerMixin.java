package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void incrementUseState(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!((OwoItemExtensions) stack.getItem()).owo$shouldTrackUsageStat() || !cir.getReturnValue().shouldIncrementStat()) {
            return;
        }

        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
    }

}
