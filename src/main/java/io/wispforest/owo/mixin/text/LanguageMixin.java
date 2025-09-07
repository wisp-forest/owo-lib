package io.wispforest.owo.mixin.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.Owo;
import io.wispforest.owo.text.LanguageAccess;
import io.wispforest.owo.text.NestedLangHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Language;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Debug(export = true)
@Mixin(Language.class)
public class LanguageMixin {
    @Shadow @Final private static Logger LOGGER;
    @Unique private static boolean skipNext;

    @WrapOperation(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;entrySet()Ljava/util/Set;"))
    private static Set<Map.Entry<String, JsonElement>> deNestNestedKeys(JsonObject instance, Operation<Set<Map.Entry<String, JsonElement>>> original) {
        var key = "owo:nested_lang";
        if (instance.has(key) && instance.get(key).isJsonPrimitive() && instance.get(key).getAsBoolean()) {
            instance.remove(key);
            return NestedLangHandler.deNest(original.call(instance));
        }
        return original.call(instance);
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

    @WrapOperation(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;asString(Lcom/google/gson/JsonElement;Ljava/lang/String;)Ljava/lang/String;"))
    private static String preventThrowingOnError(
        JsonElement element,
        String name,
        Operation<String> original,
        @Share("owo_language_key_loading_error") LocalBooleanRef failed
    ) {
        try {
            failed.set(false);
            return original.call(element, name);
        } catch (Exception e) {
            failed.set(true);
            Owo.LOGGER.warn("Preventing language loading from failing due to invalid key \"{}\"\n{}", name, e.getMessage());
            return "";
        }
    }

    @WrapWithCondition(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static <T, U> boolean preventThrowingOnError$part2(
        BiConsumer<T,U> instance, T t, U u, @Share("owo_language_key_loading_error") LocalBooleanRef failed
    ) {
        return !failed.get();
    }
}
