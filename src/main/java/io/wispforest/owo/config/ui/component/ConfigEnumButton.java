package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

@ApiStatus.Internal
public class ConfigEnumButton extends ButtonWidget implements OptionComponent {

    @Nullable protected Option<? extends Enum<?>> backingOption = null;
    @Nullable protected Enum<?>[] backingValues = null;
    protected int selectedIndex = 0;

    protected boolean wasRightClicked = false;

    public ConfigEnumButton() {
        super(0, 0, 0, 0, Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.updateMessage();
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        this.wasRightClicked = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        if (this.wasRightClicked || Screen.hasShiftDown()) {
            this.selectedIndex--;
            if (this.selectedIndex < 0) this.selectedIndex += this.backingValues.length;
        } else {
            this.selectedIndex++;
            if (this.selectedIndex > this.backingValues.length - 1) this.selectedIndex -= this.backingValues.length;
        }

        this.updateMessage();

        super.onPress();
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || super.isValidClickButton(button);
    }

    protected void updateMessage() {
        if (this.backingOption == null) return;

        this.setMessage(Text.translatable(
                this.backingOption.translationKey() + ".value." + this.backingValues[this.selectedIndex].name().toLowerCase(Locale.ROOT)
        ));
    }

    public ConfigEnumButton init(Option<? extends Enum<?>> option, int selectedIndex) {
        this.backingOption = option;
        this.backingValues = (Enum<?>[]) option.backingField().field().getType().getEnumConstants();
        this.selectedIndex = selectedIndex;

        this.updateMessage();

        return this;
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
