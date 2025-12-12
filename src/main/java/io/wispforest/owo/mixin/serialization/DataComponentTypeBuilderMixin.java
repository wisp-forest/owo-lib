package io.wispforest.owo.mixin.serialization;

import io.wispforest.owo.serialization.OwoDataComponentTypeBuilder;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentType.Builder.class)
public abstract class DataComponentTypeBuilderMixin<T> implements OwoDataComponentTypeBuilder<T> {
}
