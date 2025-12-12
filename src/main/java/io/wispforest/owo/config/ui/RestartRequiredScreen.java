package io.wispforest.owo.config.ui;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class RestartRequiredScreen extends BaseUIModelScreen<FlowLayout> {

    protected final Screen parent;

    public RestartRequiredScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Owo.id("restart_required")));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void build(FlowLayout rootComponent) {
        if (this.minecraft.level == null) {
            rootComponent.surface(Surface.optionsBackground());
        }

        rootComponent.childById(ButtonComponent.class, "exit-button")
                .onPress(button -> Minecraft.getInstance().stop());

        rootComponent.childById(ButtonComponent.class, "ignore-button")
                .onPress(button -> this.onClose());
    }
}
