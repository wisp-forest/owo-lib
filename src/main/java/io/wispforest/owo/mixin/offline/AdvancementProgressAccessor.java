package io.wispforest.owo.mixin.offline;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.AdvancementRequirements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
    @Accessor
    AdvancementRequirements getRequirements();

    @Accessor
    void setRequirements(AdvancementRequirements requirements);
}
