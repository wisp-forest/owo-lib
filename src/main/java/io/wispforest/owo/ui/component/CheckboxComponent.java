package io.wispforest.owo.ui.component;

import io.wispforest.owo.mixin.ui.CheckboxWidgetAccessor;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public class CheckboxComponent extends CheckboxWidget {

    protected final Observable<Boolean> listeners;

    protected CheckboxComponent(Text message) {
        super(0, 0, 0, 0, message, false);
        this.listeners = Observable.of(this.isChecked());
        this.sizing(Sizing.content(), Sizing.fixed(20));
    }

    @Override
    public void onPress() {
        super.onPress();
        this.listeners.set(this.isChecked());
    }

    public CheckboxComponent checked(boolean checked) {
        ((CheckboxWidgetAccessor) this).owo$setChecked(checked);
        this.listeners.set(this.isChecked());
        return this;
    }

    public CheckboxComponent onChanged(Consumer<Boolean> listener) {
        this.listeners.observe(listener);
        return this;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "checked", UIParsing::parseBool, this::checked);
        UIParsing.apply(children, "text", UIParsing::parseText, this::setMessage);
    }

    public CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.HAND;
    }
}
