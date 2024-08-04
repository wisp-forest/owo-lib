package io.wispforest.owo.mixin.offline;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;

@Mixin(targets = "net/minecraft/advancement/PlayerAdvancementTracker$ProgressMap")
public interface ProgressMapAccessor {
    @Accessor
    Map<Identifier, AdvancementProgress> getMap();
}
