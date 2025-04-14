package io.wispforest.owo.mixin.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.text.LanguageAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public class LanguageMixin {

    @Unique private static boolean skipNext;

    @WrapOperation(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;entrySet()Ljava/util/Set;"))
    private static Set<Map.Entry<String, JsonElement>> deNestNestedKeys(JsonObject instance, Operation<Set<Map.Entry<String, JsonElement>>> original) {
        return deNest("", original.call(instance));
    }

    @Unique
    private static Set<Map.Entry<String, JsonElement>> deNest(String prefix, Set<Map.Entry<String, JsonElement>> entries) {
        var returned = new HashSet<Map.Entry<String, JsonElement>>();
        for (var entry : entries) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (value.isJsonObject() && key.endsWith("..")) {
                returned.addAll(deNest(prefix + key.substring(0, key.length() - 2), value.getAsJsonObject().entrySet()));
            } else {
                returned.add(Map.entry(prefix + key, value));
            }
        }
        return returned;
    }

    @WrapOperation(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;asString(Lcom/google/gson/JsonElement;Ljava/lang/String;)Ljava/lang/String;"))
    private static String skipIfObjectOrArray(JsonElement element, String name, Operation<String> original) {
        if (!element.isJsonPrimitive() && LanguageAccess.textConsumer != null) {
            skipNext = true;

            MutableText text = (MutableText) TextCodecs.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(JsonParseException::new);
            LanguageAccess.textConsumer.accept(name, text);

            return "";
        } else if (element.isJsonPrimitive()) {
            skipNext = false;
            return original.call(element, name);
        } else {
            skipNext = true;
            return "";
        }
    }

    @WrapWithCondition(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static boolean doSkip(BiConsumer<Object, Object> biConsumer, Object t, Object u) {
        return !skipNext;
    }
}
