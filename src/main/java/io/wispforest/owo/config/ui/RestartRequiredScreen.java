package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class RestartRequiredScreen extends BaseUIModelScreen<FlowLayout> {

    protected final Screen parent;

    public RestartRequiredScreen(Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo", "restart_required")));
        this.parent = parent;
    }


    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(ButtonWidget.class, "exit-button")
                .onPress(button -> MinecraftClient.getInstance().scheduleStop());

        rootComponent.childById(ButtonWidget.class, "ignore-button")
                .onPress(button -> this.close());
    }
}
