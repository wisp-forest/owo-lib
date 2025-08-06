package io.wispforest.owo.mixin.neoforge;

import com.mojang.blaze3d.systems.CommandEncoder;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ValidationCommandEncoder.class)
public interface ValidationCommandEncoderAccessor {
    @Accessor("realCommandEncoder")
    CommandEncoder owo$getRealCommandEncoder();
}
