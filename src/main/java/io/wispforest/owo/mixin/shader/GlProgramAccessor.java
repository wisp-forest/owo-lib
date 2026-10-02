package io.wispforest.owo.mixin.shader;

import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.Uniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

/** Public shader-uniform accessor, backed by the higher-priority required map mixin. */
@Mixin(GlProgram.class)
public interface GlProgramAccessor {
    @Accessor("owo$uniformsByName")
    Map<String, Uniform> owo$getUniformsByName();
}
