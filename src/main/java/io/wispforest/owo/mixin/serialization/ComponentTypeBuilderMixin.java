package io.wispforest.owo.mixin.serialization;

import io.wispforest.owo.serialization.OwoComponentTypeBuilder;
import net.minecraft.component.ComponentType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComponentType.Builder.class)
public abstract class ComponentTypeBuilderMixin<T> implements OwoComponentTypeBuilder<T> {
}
