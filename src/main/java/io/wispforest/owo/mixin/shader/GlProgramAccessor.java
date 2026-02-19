package io.wispforest.owo.mixin.shader;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.Uniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GlProgram.class)
public interface GlProgramAccessor {

    @Accessor("uniformsByName")
    Map<String, Uniform> owo$getUniformsByName();

}
