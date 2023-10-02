package io.wispforest.owo.mixin.offline;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.criterion.CriterionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
    @Accessor
    AdvancementRequirements getRequirements();

    @Accessor
    void setRequirements(AdvancementRequirements requirements);
}