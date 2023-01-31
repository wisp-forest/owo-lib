package io.wispforest.owo.mixin.shader;

import io.wispforest.owo.client.OwoClient;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShaderProgram.class)
public class ShaderProgramMixin {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), require = 0)
    private String fixIdentifier(String id) {
        if (OwoClient.SATIN_LOADED) return id;

        var splitName = id.split(":");
        if (splitName.length != 2) return id;

        return splitName[0].replace("shaders/core/", "") + ":" + "shaders/core/" + splitName[1];
    }

    @ModifyArg(method = "loadShader", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), require = 0)
    private static String fixMoreIdentifiers(String id) {
        if (OwoClient.SATIN_LOADED) return id;

        var splitName = id.split(":");
        if (splitName.length != 2) return id;

        return splitName[0].replace("shaders/core/", "") + ":" + "shaders/core/" + splitName[1];
    }

}
