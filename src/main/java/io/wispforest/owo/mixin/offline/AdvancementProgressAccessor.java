package io.wispforest.owo.mixin.offline;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.criterion.CriterionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
    @Accessor
    Map<String, CriterionProgress> getCriteriaProgresses();

    @Accessor
    String[][] getRequirements();

    @Accessor
    void setRequirements(String[][] requirements);
}