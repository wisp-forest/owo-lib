package io.wispforest.owo.mixin.offline;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
    @Accessor
    AdvancementRequirements getRequirements();

    @Accessor
    void setRequirements(AdvancementRequirements requirements);
}