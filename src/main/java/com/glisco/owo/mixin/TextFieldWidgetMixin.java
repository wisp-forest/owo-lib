package com.glisco.owo.mixin;

import com.glisco.owo.Owo;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {

    @Shadow
    private String text;

    @Inject(method = "getWordSkipPosition(IIZ)I", at = @At("HEAD"), cancellable = true)
    private void iProvideUsefulSeparators(int wordOffset, int cursorPosition, boolean skipOverSpaces, CallbackInfoReturnable<Integer> cir) {
        if (!Owo.DEBUG) return;

        int wordsToSkip = Math.abs(wordOffset);
        boolean forward = wordOffset > 0;

        for (int i = 0; i < wordsToSkip; i++) {
            if (forward) {
                cursorPosition++;
                while (cursorPosition < this.text.length() && owo$isWordChar(this.text.charAt(cursorPosition))) cursorPosition++;
            } else {
                cursorPosition--;
                while (cursorPosition > 0 && owo$isWordChar(this.text.charAt(cursorPosition - 1))) cursorPosition--;
            }
        }

        cir.setReturnValue(cursorPosition);
    }

    @Unique
    private boolean owo$isWordChar(char charAt) {
        return charAt == '_' || Character.isAlphabetic(charAt);
    }

}
