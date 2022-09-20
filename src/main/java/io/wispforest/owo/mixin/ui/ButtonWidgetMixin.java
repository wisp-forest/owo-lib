package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.inject.ButtonWidgetExtension;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("ConstantConditions")
@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin extends ClickableWidget implements ButtonWidgetExtension {
    @Mutable
    @Shadow
    @Final
    protected ButtonWidget.PressAction onPress;

    public ButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public ButtonWidget onPress(ButtonWidget.PressAction pressAction) {
        this.onPress = pressAction;
        return (ButtonWidget) (Object) this;
    }
}
