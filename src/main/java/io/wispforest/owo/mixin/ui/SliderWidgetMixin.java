package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SuppressWarnings("ConstantConditions")
@Mixin(AbstractSliderButton.class)
public abstract class SliderWidgetMixin extends AbstractWidget {
    @Shadow
    protected abstract void setValue(double value);

    @Shadow protected double value;

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @ModifyArg(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/SliderWidget;setValue(D)V"))
    private double injectCustomStep(double value) {
        if (!((Object) this instanceof SliderComponent slider)) return value;
        return this.value + Math.signum(value - this.value) * slider.scrollStep();
    }

    @Inject(method = "setValueFromMouse", at = @At("HEAD"), cancellable = true)
    private void makeItSnappyTeam(double mouseX, CallbackInfo ci) {
        if (!((Object) this instanceof DiscreteSliderComponent discrete)) return;
        if (!discrete.snap()) return;

        ci.cancel();

        double value = (mouseX - (this.getX() + 4d)) / (this.width - 8d);
        double min = discrete.min(), max = discrete.max();
        int decimalPlaces = discrete.decimalPlaces();

        this.setValue(
                (new BigDecimal(min + value * (max - min)).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue() - min) / (max - min)
        );
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.MOVE;
    }
}