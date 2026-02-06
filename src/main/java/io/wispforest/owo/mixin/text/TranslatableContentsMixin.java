package io.wispforest.owo.mixin.text;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.Owo;
import io.wispforest.owo.text.CursedTranslatableContents;
import io.wispforest.owo.text.InsertingTextContent;
import io.wispforest.owo.text.TextLanguage;
import io.wispforest.owo.text.TranslationContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
    @Shadow private List<FormattedText> decomposedParts;

    @Shadow
    @Final
    private String key;

    @Inject(method = {"visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;", "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;"}, at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), cancellable = true)
    private <T> void enter(CallbackInfoReturnable<Optional<T>> cir) {
        if (!TranslationContext.pushContent((TranslatableContents) (Object) this)) {
            Owo.LOGGER.warn("Detected translation reference cycle, replacing with empty");
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method = {"visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;", "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;"}, at = @At(value = "RETURN"))
    private <T> void exit(CallbackInfoReturnable<Optional<T>> cir) {
        TranslationContext.popContent();
    }

    @Inject(method = "decompose", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;)Ljava/lang/String;"), cancellable = true)
    private void pullTranslationText(CallbackInfo ci) {
        Language lang = Language.getInstance();
        if (lang instanceof TextLanguage) {
            Component text = ((TextLanguage) lang).getText(key);

            if (text != null) {
                decomposedParts = new ArrayList<>();
                decomposedParts.add(text);
                ci.cancel();
            }
        }
    }

    @ModifyVariable(
        method = "decomposeTemplate",
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    private int restoreCorrectArgIndex(int value) {
        return ((Object)this) instanceof CursedTranslatableContents ? CursedTranslatableContents.argIndex : value;
    }

    @Inject(
        method = "decomposeTemplate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/contents/TranslatableContents;getArgument(I)Lnet/minecraft/network/chat/FormattedText;"
        )
    )
    private void keepCorrectArgIndex(CallbackInfo ci, @Local(ordinal = 0) int argIndex) {
        if (((Object)this) instanceof CursedTranslatableContents) CursedTranslatableContents.argIndex = argIndex;
    }

    @WrapMethod(method = "getArgument")
    private FormattedText unpackArgs(int index, Operation<FormattedText> original) {
        return ((Object)this) instanceof CursedTranslatableContents ? MutableComponent.create(new InsertingTextContent(index)) : original.call(index);
    }
}
