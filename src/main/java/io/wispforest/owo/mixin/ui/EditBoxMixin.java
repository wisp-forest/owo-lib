package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import io.wispforest.owo.mixin.ui.access.TextBoxComponentAccessor;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.inject.GreedyInputUIComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget implements GreedyInputUIComponent {

    @Shadow private String value;
    public EditBoxMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "onValueChange", at = @At("HEAD"))
    private void callOwoListener(String newText, CallbackInfo ci) {
        if (!(this instanceof TextBoxComponentAccessor accessor)) return;
        accessor.owo$textValue().set(newText);
    }

    @Override
    public void onFocusGained(FocusSource source) {
        super.onFocusGained(source);
        this.setFocused(true);
    }

    @ModifyExpressionValue(method = "insertText", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"))
    private String injectFilter(String original, @Cancellable CallbackInfo ci) {
        if (!((Object) this instanceof TextBoxComponent textBox) || textBox.getFilter().test(original)) {
            return original;
        }

        ci.cancel();
        return this.value;
    }

    @ModifyExpressionValue(method = "deleteCharsToPos", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"))
    private String injectFilterButDoItAgain(String original, @Cancellable CallbackInfo ci) {
        if (!((Object) this instanceof TextBoxComponent textBox) || textBox.getFilter().test(original)) {
            return original;
        }

        ci.cancel();
        return this.value;
    }
}
