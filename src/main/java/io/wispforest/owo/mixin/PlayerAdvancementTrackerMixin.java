package io.wispforest.owo.mixin;

import io.wispforest.owo.offline.PlayerAdvancementsSaved;
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
    @Shadow private ServerPlayerEntity owner;

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "save", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;toJsonTree(Ljava/lang/Object;)Lcom/google/gson/JsonElement;"))
    private Object onAdvancementsSaved(Object map) {
        PlayerAdvancementsSaved.EVENT.invoker().onPlayerAdvancementsSaved(owner.getUuid(), (Map<Identifier, AdvancementProgress>)map);
        return map;
    }
}
