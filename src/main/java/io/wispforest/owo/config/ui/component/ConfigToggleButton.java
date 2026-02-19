package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConfigToggleButton extends ButtonComponent implements OptionValueProvider {

    protected static final Component ENABLED_MESSAGE = Component.translatable("text.owo.config.boolean_toggle.enabled");
    protected static final Component DISABLED_MESSAGE = Component.translatable("text.owo.config.boolean_toggle.disabled");

    protected boolean enabled = false;

    public ConfigToggleButton() {
        super(Component.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.updateMessage();
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.enabled = !this.enabled;
        this.updateMessage();
        super.onPress(input);
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
