package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.ui.ConfigTranslationHelper;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@ApiStatus.Internal
public class ConfigEnumButton extends ButtonComponent implements OptionValueProvider {

    @Nullable protected OptionControlSpec<? extends Enum<?>> backingOption = null;
    @Nullable protected Enum<?>[] backingValues = null;
    protected int selectedIndex = 0;

    protected boolean wasRightClicked = false;

    public ConfigEnumButton() {
        super(Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.updateMessage();
    }

    @Override
    public boolean onMouseDown(Click click, boolean doubled) {
        this.wasRightClicked = click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        return super.onMouseDown(click, doubled);
    }

    @Override
    public void onPress(AbstractInput input) {
        if (this.wasRightClicked || input.hasShift()) {
            this.selectedIndex--;
            if (this.selectedIndex < 0) this.selectedIndex += this.backingValues.length;
        } else {
            this.selectedIndex++;
            if (this.selectedIndex > this.backingValues.length - 1) this.selectedIndex -= this.backingValues.length;
        }

        this.updateMessage();

        super.onPress(input);
    }

    @Override
    protected boolean isValidClickButton(MouseInput input) {
        return input.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT || super.isValidClickButton(input);
    }

    protected void updateMessage() {
        if (this.backingOption == null) return;

        var enumName = StringUtils.uncapitalize(this.backingValues.getClass().componentType().getSimpleName());

        this.setMessage(Text.translatable(ConfigTranslationHelper.createEnumTranslation(this.backingOption.key(), this.backingValues, this.selectedIndex)));
    }

    public ConfigEnumButton init(OptionControlSpec<? extends Enum<?>> option, int selectedIndex) {
        this.backingOption = option;
        this.backingValues = option.clazz().getEnumConstants();

        return select(selectedIndex);
    }

    public ConfigEnumButton select(int index) {
        this.selectedIndex = index;
        this.updateMessage();

        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        return this.backingValues[this.selectedIndex];
    }
}
