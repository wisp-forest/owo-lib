package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.OwoComponentTypeBuilder;
import net.minecraft.component.ComponentType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComponentType.Builder.class)
public class ComponentTypeBuilderMixin<T> implements OwoComponentTypeBuilder<T> {
}
