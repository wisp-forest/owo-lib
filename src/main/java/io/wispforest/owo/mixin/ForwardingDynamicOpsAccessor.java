package io.wispforest.owo.mixin;

import com.mojang.serialization.DynamicOps;
import net.minecraft.util.dynamic.ForwardingDynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ForwardingDynamicOps.class)
public interface ForwardingDynamicOpsAccessor<T> {
    @Accessor("delegate")
    DynamicOps<T> owo$delegate();
}
