package io.wispforest.owo.mixin.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.Owo;
import io.wispforest.owo.text.CursedTranslatableContents;
import io.wispforest.owo.text.LanguageAccess;
import io.wispforest.owo.text.NestedLangHandler;
import io.wispforest.owo.util.DataExtensionUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public class LanguageMixin {

    @Unique private static final String RICH_TRANSLATIONS_ENABLER = "rich_translations";
    @Unique private static final String NESTED_LANG_ENABLER = "nested_lang";

    @WrapOperation(
        method = "loadFromJson(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V",
        at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;entrySet()Ljava/util/Set;")
    )
    private static Set<Map.Entry<String, JsonElement>> deNestNestedKeys(
        JsonObject instance,
        Operation<Set<Map.Entry<String, JsonElement>>> original,
        @Share(RICH_TRANSLATIONS_ENABLER) LocalBooleanRef richTranslationsEnabled,
        @Share(NESTED_LANG_ENABLER) LocalBooleanRef nestedLangEnabled,
        @Local(argsOnly = true) InputStream stream
    ) {
        var enabledByDefault = featureEnabled(instance, "extended_lang", false) || stream instanceof DataExtensionUtil.CoercedByteArrayInputStream;
        richTranslationsEnabled.set(featureEnabled(instance, RICH_TRANSLATIONS_ENABLER, enabledByDefault));
        nestedLangEnabled.set(featureEnabled(instance, NESTED_LANG_ENABLER, enabledByDefault));
        return nestedLangEnabled.get() ? NestedLangHandler.deNest(original.call(instance)) : original.call(instance);
    }

    @Unique
    private static boolean featureEnabled(JsonObject instance, String feature, boolean defaultValue) {
        feature = Owo.id(feature).toString();
        var value = instance.get(feature);
        if (!(value instanceof JsonPrimitive primitive)) return defaultValue;
        instance.remove(feature);
        if (primitive.isNumber()) return primitive.getAsNumber().doubleValue() != 0;
        return primitive.getAsBoolean();
    }

    @Unique private static final String SKIP_NEXT = "skipNextKey";

    @WrapOperation(
        method = "loadFromJson(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/util/GsonHelper;convertToString(Lcom/google/gson/JsonElement;Ljava/lang/String;)Ljava/lang/String;"
    )
    )
    private static String handleRichTranslationsAndErrors(
        JsonElement element,
        String name,
        Operation<String> original,
        @Share(RICH_TRANSLATIONS_ENABLER) LocalBooleanRef richTranslationsEnabled,
        @Share(NESTED_LANG_ENABLER) LocalBooleanRef nestedLangEnabled,
        @Share(SKIP_NEXT) LocalBooleanRef skipNext
    ) {
        skipNext.set(false);
        var rich = richTranslationsEnabled.get();
        if (rich || nestedLangEnabled.get()) {
            try {
                var consumer = LanguageAccess.textConsumer.get();
                if (rich && !element.isJsonPrimitive() && consumer != LanguageAccess.EMPTY_CONSUMER) {
                    skipNext.set(true);

                    MutableComponent text = (MutableComponent) ComponentSerialization.CODEC
                        .parse(JsonOps.INSTANCE, element)
                        .getOrThrow(JsonParseException::new);
                    consumer.accept(name, CursedTranslatableContents.unpackArgs(text));

                    return "";
                } else if (element.isJsonPrimitive()) {
                    return original.call(element, name);
                } else {
                    skipNext.set(true);
                    return "";
                }
            } catch (Exception e) {
                skipNext.set(true);
                Owo.LOGGER.error(
                    "Preventing language loading from failing due to invalid key \"{}\"\n{}",
                    name,
                    e.getMessage()
                );
                return "";
            }
        }
        return original.call(element, name);
    }

    @WrapWithCondition(
        method = "loadFromJson(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static boolean doSkip(
        BiConsumer<Object, Object> biConsumer,
        Object t, Object u,
        @Share(SKIP_NEXT) LocalBooleanRef skipNext
    ) {
        return !skipNext.get();
    }
}
