package io.wispforest.owo.mixin.shader;

import ;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderInstance.class)
public class ShaderProgramMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"), require = 0)
    private Identifier fixIdentifier(String path, Operation<Identifier> original, @Local(argsOnly = true) String name) {
        if ((Object) this instanceof GlProgram.OwoShaderProgram) {
            var pathParts = path.split(name);
            if (pathParts.length == 2 && pathParts[0].startsWith("shaders/core/")) {
                var programParts = name.split(":");

                return Identifier.of(programParts[0], pathParts[0] + programParts[1] + pathParts[1]);
            }
        }

        return original.call(path);
    }

}
