package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.OwoComponentTypeBuilder;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentType.Builder.class)
public class DataComponentTypeBuilderMixin<T> implements OwoComponentTypeBuilder<T> {
}
