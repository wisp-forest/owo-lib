package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

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

    public CheckboxComponent onChanged(Consumer<Boolean> listener) {
        this.listeners.observe(listener);
        return this;
    }
}
