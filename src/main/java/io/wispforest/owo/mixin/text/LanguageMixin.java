package io.wispforest.owo.mixin.text;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(Language.class)
public class LanguageMixin {
    @ModifyExpressionValue(method = "loadFromJson", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonElement;isJsonArray()Z"))
    private static boolean widenScope(boolean original, @Local Map.Entry<String, JsonElement> entry) {
        return original | !entry.getValue().isJsonPrimitive();
    }
}
