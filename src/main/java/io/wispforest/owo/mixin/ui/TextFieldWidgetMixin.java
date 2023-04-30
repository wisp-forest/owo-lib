package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.mixin.ui.access.TextBoxComponentAccessor;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget implements GreedyInputComponent {

    public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "onChanged", at = @At("HEAD"))
    private void callOwoListener(String newText, CallbackInfo ci) {
        if (!(this instanceof TextBoxComponentAccessor accessor)) return;
        accessor.owo$textValue().set(newText);
    }

    @Override
    public void onFocusGained(FocusSource source) {
        super.onFocusGained(source);
        this.setFocused(true);
    }

}
