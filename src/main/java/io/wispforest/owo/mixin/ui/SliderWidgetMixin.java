package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {
    @Shadow
    protected abstract void setValue(double value);

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "setValueFromMouse", at = @At("HEAD"), cancellable = true)
    private void makeItSnappyTeam(double mouseX, CallbackInfo ci) {
        if (!((Object) this instanceof DiscreteSliderComponent discrete)) return;
        if (!discrete.snap()) return;

        ci.cancel();

        double value = (mouseX - (this.x + 4d)) / (this.width - 8d);
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