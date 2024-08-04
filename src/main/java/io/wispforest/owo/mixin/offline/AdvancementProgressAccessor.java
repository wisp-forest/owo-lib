package io.wispforest.owo.mixin.offline;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;

@Mixin(AdvancementProgress.class)
public interface AdvancementProgressAccessor {
    @Accessor
    AdvancementRequirements getRequirements();

    @Accessor
    void setRequirements(AdvancementRequirements requirements);
}