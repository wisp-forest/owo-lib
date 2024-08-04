package io.wispforest.owo.mixin.offline;

import io.wispforest.owo.offline.DataSavedEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayer owner;

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "save", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encodeStart(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", remap = false), index = 1)
    private Object onAdvancementsSaved(Object map) {
        DataSavedEvents.ADVANCEMENTS.invoker().onSaved(owner.getUuid(), ((ProgressMapAccessor) map).getMap());
        return map;
    }
}
