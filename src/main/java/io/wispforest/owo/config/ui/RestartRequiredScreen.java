package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class RestartRequiredScreen extends BaseUIModelScreen<FlowLayout> {

    protected final Screen parent;

    public RestartRequiredScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.of("owo", "restart_required")));
        this.parent = parent;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void build(FlowLayout rootComponent) {
        if (this.client.world == null) {
            rootComponent.surface(Surface.OPTIONS_BACKGROUND);
        }

        rootComponent.childById(ButtonComponent.class, "exit-button")
                .onPress(button -> MinecraftClient.getInstance().scheduleStop());

        rootComponent.childById(ButtonComponent.class, "ignore-button")
                .onPress(button -> this.close());
    }
}
