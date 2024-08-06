package io.wispforest.owo.mixin.text;

import io.wispforest.owo.Owo;
import io.wispforest.owo.text.TextLanguage;
import io.wispforest.owo.text.TranslationContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Text;
import net.minecraft.network.chat.contents.TranslatableContents;
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
            Text text = ((TextLanguage) lang).getText(key);

            if (text != null) {
                decomposedParts = new ArrayList<>();
                decomposedParts.add(text);
                ci.cancel();
            }
        }
    }
}
