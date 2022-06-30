package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.definitions.CursorStyle;
import io.wispforest.owo.ui.parsing.OwoUIParsing;
import io.wispforest.owo.ui.parsing.OwoUISpec;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.w3c.dom.Element;

import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget {

    public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        OwoUIParsing.apply(children, "text", e -> e.getTextContent().strip(), text -> {
            ((TextFieldWidget) (Object) this).setText(text);
            ((TextFieldWidget) (Object) this).setCursorToStart();
        });
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

    @Override
    public CursorStyle cursorStyle() {
        return CursorStyle.TEXT;
    }
}
