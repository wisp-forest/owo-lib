package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConfigToggleButton extends ButtonComponent implements OptionValueProvider {

    protected static final Text ENABLED_MESSAGE = Text.translatable("text.owo.config.boolean_toggle.enabled");
    protected static final Text DISABLED_MESSAGE = Text.translatable("text.owo.config.boolean_toggle.disabled");

    protected boolean enabled = false;

    public ConfigToggleButton() {
        super(Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.updateMessage();
    }

    @Override
    public void onPress() {
        this.enabled = !this.enabled;
        this.updateMessage();
        super.onPress();
    }

    protected void updateMessage() {
        this.setMessage(this.enabled ? ENABLED_MESSAGE : DISABLED_MESSAGE);
    }

    public ConfigToggleButton enabled(boolean enabled) {
        this.enabled = enabled;
        this.updateMessage();
        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        return this.enabled;
    }
}
