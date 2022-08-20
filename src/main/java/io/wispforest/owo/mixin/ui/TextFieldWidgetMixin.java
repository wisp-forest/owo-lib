package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.w3c.dom.Element;

import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget {

    @Shadow public abstract void setDrawsBackground(boolean drawsBackground);

    public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public void drawFocusHighlight(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        // noop, since TextFieldWidget already does this
    }

    @Override
    public void parseProperties(UIModel spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        UIParsing.apply(children, "text", e -> e.getTextContent().strip(), text -> {
            ((TextFieldWidget) (Object) this).setText(text);
            ((TextFieldWidget) (Object) this).setCursorToStart();
        });
        UIParsing.apply(children, "show-background", UIParsing::parseBool, this::setDrawsBackground);
    }

    @SuppressWarnings("ReferenceToMixin")
    @Inject(method = "setX", at = @At("HEAD"), cancellable = true)
    private void applyToWrapper(int x, CallbackInfo ci) {
        final var wrapper = ((ClickableWidgetMixin) (Object) this).owo$wrapper;
        if (wrapper != null) {
            wrapper.setX(x);
            ci.cancel();
        }
    }

    @SuppressWarnings("ReferenceToMixin")
    protected void owo$initializeWrapper() {
        ((ClickableWidgetMixin)(Object)this).owo$wrapper.cursorStyle(CursorStyle.TEXT);
    }
}
