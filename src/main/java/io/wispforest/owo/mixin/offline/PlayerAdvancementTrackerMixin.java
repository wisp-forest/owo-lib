package io.wispforest.owo.mixin.offline;

import io.wispforest.owo.offline.DataSavedEvents;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "save", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", remap = false), index = 1)
    private Object onAdvancementsSaved(Object map) {
        DataSavedEvents.ADVANCEMENTS.invoker().onSaved(owner.getUuid(), ((ProgressMapAccessor) map).getMap());
        return map;
    }
}
