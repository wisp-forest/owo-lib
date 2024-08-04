package io.wispforest.owo.mixin.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.blaze3d.shaders.Uniform;
import java.util.Map;
import net.minecraft.client.renderer.ShaderInstance;

@Mixin(ShaderInstance.class)
public interface ShaderProgramAccessor {

    @Accessor("loadedUniforms")
    Map<String, Uniform> owo$getLoadedUniforms();

}
