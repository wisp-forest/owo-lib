package io.wispforest.owo.mixin;

import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.DelegatingOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DelegatingOps.class)
public interface DelegatingOpsAccessor<T> {
    @Accessor("delegate")
    DynamicOps<T> owo$delegate();
}
