package io.wispforest.owo.mixin.shader;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.Uniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = GlProgram.class, priority = 1100)
public abstract class GlProgramMixin {
    @Shadow public abstract Uniform getUniform(int index);
    @Unique private final Map<String, Uniform> owo$uniformsByName = new HashMap<>();

    @Inject(method = "setupBindGroupLayouts", at = @At("TAIL"))
    private void owo$captureUniformNames(List<BindGroupLayout.UniformDescription> descriptions, CallbackInfo ci) {
        this.owo$uniformsByName.clear();
        for (int index = 0; index < descriptions.size(); index++) {
            var uniform = this.getUniform(index);
            if (uniform != null) this.owo$uniformsByName.put(descriptions.get(index).name(), uniform);
        }
    }
}
