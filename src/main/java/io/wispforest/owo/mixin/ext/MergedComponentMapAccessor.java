package io.wispforest.owo.mixin.ext;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MergedComponentMap.class)
public interface MergedComponentMapAccessor {
    @Accessor("baseComponents")
    ComponentMap owo$getBaseComponents();

    @Accessor("baseComponents")
    @Mutable
    void owo$setBaseComponents(ComponentMap baseComponents);
}
