package io.wispforest.owo.mixin.text;

import io.wispforest.owo.Owo;
import io.wispforest.owo.text.TextLanguage;
import io.wispforest.owo.text.TranslationContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Text;
import net.minecraft.network.chat.contents.TranslatableContents;

@Mixin(TranslatableContents.class)
public class TranslatableTextContentMixin {
    @Shadow private List<FormattedText> translations;

    @Shadow
    @Final
    private String key;

    @Inject(method = {"visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;", "visit(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;"}, at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), cancellable = true)
    private <T> void enter(CallbackInfoReturnable<Optional<T>> cir) {
        if (!TranslationContext.pushContent((TranslatableContents) (Object) this)) {
            Owo.LOGGER.warn("Detected translation reference cycle, replacing with empty");
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method = {"visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;", "visit(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;"}, at = @At(value = "RETURN"))
    private <T> void exit(CallbackInfoReturnable<Optional<T>> cir) {
        TranslationContext.popContent();
    }

    @Inject(method = "updateTranslations", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Language;get(Ljava/lang/String;)Ljava/lang/String;"), cancellable = true)
    private void pullTranslationText(CallbackInfo ci) {
        Language lang = Language.getInstance();
        if (lang instanceof TextLanguage) {
            Text text = ((TextLanguage) lang).getText(key);

            if (text != null) {
                translations = new ArrayList<>();
                translations.add(text);
                ci.cancel();
            }
        }
    }
}
