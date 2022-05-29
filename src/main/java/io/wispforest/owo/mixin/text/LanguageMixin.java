package io.wispforest.owo.mixin.text;

import com.google.gson.JsonElement;
import io.wispforest.owo.text.LanguageAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public class LanguageMixin {

    @Unique private static boolean skipNext;

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;asString(Lcom/google/gson/JsonElement;Ljava/lang/String;)Ljava/lang/String;"))
    private static String skipIfObjectOrArray(JsonElement el, String str, InputStream inputStream, BiConsumer<String, String> entryConsumer) {
        if (!el.isJsonPrimitive() && LanguageAccess.textConsumer != null) {
            skipNext = true;

            MutableText text = Text.Serializer.fromJson(el);
            LanguageAccess.textConsumer.accept(str, text);

            return "";
        } else {
            skipNext = false;
            return JsonHelper.asString(el, str);
        }
    }

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static void doSkip(BiConsumer<Object, Object> biConsumer, Object t, Object u) {
        if (!skipNext)
            biConsumer.accept(t, u);
    }
}
