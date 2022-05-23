package io.wispforest.owo.mixin;

import com.google.gson.*;
import io.wispforest.owo.text.CustomTextContent;
import io.wispforest.owo.text.CustomTextRegistry;
import io.wispforest.owo.text.CustomTextContentSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.Map;

@Mixin(Text.Serializer.class)
public abstract class TextSerializerMixin {
    @Shadow
    public abstract MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException;

    @Shadow protected abstract void addStyle(Style style, JsonObject json, JsonSerializationContext context);

    @Shadow public abstract JsonElement serialize(Text text, Type type, JsonSerializationContext jsonSerializationContext);

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/text/MutableText;", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;has(Ljava/lang/String;)Z"), cancellable = true)
    private void deserializeCustomText(JsonElement el, Type type, JsonDeserializationContext ctx, CallbackInfoReturnable<MutableText> cir) {
        JsonObject obj = el.getAsJsonObject();

        for (Map.Entry<String, CustomTextContentSerializer<?>> entry : CustomTextRegistry.serializerMap().entrySet()) {
            if (obj.has(entry.getKey())) {
                MutableText text = MutableText.of(entry.getValue().deserialize(obj, ctx));

                if (el.getAsJsonObject().has("extra")) {
                    JsonArray extra = JsonHelper.getArray(obj, "extra");
                    if (extra.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for (int j = 0; j < extra.size(); ++j) {
                        text.append(deserialize(extra.get(j), type, ctx));
                    }
                }

                text.setStyle(ctx.deserialize(el, Style.class));

                cir.setReturnValue(text);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    @Inject(method = "serialize(Lnet/minecraft/text/Text;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;", at = @At("HEAD"), cancellable = true)
    private void serializeCustomText(Text text, Type type, JsonSerializationContext ctx, CallbackInfoReturnable<JsonElement> cir) {
        if (!(text instanceof CustomTextContent custom))
            return;

        JsonObject obj = new JsonObject();

        ((CustomTextContentSerializer<CustomTextContent>) custom.serializer()).serialize(custom, obj, ctx);

        if (!text.getStyle().isEmpty()) {
            addStyle(text.getStyle(), obj, ctx);
        }

        if (!text.getSiblings().isEmpty()) {
            JsonArray siblings = new JsonArray();
            for (Text sibling : text.getSiblings()) {
                siblings.add(serialize(sibling, sibling.getClass(), ctx));
            }

            obj.add("extra", siblings);
        }

        cir.setReturnValue(obj);
    }
}

