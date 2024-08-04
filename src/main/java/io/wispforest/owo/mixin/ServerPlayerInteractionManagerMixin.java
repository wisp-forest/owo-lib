package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void incrementUseState(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!((OwoItemExtensions) stack.getItem()).owo$shouldTrackUsageStat() || !cir.getReturnValue().indicateItemUse()) {
            return;
        }

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    }

}
