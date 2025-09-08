package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.impl.OwoItemGroupImpl;
import io.wispforest.owo.itemgroup.OwoItemGroupBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OwoItemGroupBuilder.class)
public interface OwoItemGroupBuilderAccessor {
    @Invoker("build")
    OwoItemGroupImpl owo$build();
}
