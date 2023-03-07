package io.wispforest.owo.mixin.tweaks;

import io.wispforest.owo.Owo;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget {

    @Shadow
    private String text;

    public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

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
        return charAt == '_' || Character.isAlphabetic(charAt) || Character.isDigit(charAt);
    }

}
