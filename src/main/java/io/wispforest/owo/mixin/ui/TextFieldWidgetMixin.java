package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.inject.GreedyInputComponent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin implements GreedyInputComponent {

    @Inject(method = "onChanged", at = @At("HEAD"))
    private void callOwoListener(String newText, CallbackInfo ci) {
        if (!(this instanceof TextBoxComponentAccessor accessor)) return;
        accessor.owo$textValue().set(newText);
    }

}
