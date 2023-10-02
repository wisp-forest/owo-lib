package io.wispforest.owo.mixin.offline;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(targets = "net/minecraft/advancement/PlayerAdvancementTracker$ProgressMap")
public interface ProgressMapAccessor {
    @Accessor
    Map<Identifier, AdvancementProgress> getMap();
}
