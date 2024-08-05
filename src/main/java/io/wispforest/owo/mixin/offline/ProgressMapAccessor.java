package io.wispforest.owo.mixin.offline;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(targets = "net/minecraft/server/PlayerAdvancements$Data")
public interface ProgressMapAccessor {
    @Accessor
    Map<Identifier, AdvancementProgress> getMap();
}
