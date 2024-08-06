package io.wispforest.owo.mixin.shader;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ShaderInstance.class)
public interface ShaderInstanceAccessor {

    @Accessor("uniformMap")
    Map<String, Uniform> owo$getUniformMap();

}
