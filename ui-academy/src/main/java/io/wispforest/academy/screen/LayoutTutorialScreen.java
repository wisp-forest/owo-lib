package io.wispforest.academy.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class LayoutTutorialScreen extends BaseUIModelScreen<FlowLayout> {

    private final Screen parent;

    public LayoutTutorialScreen(@Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo-ui-academy", "layout")));
        this.parent = parent;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(ButtonWidget.class, "inspector-button")
                .onPress(button -> this.uiAdapter.toggleInspector());

        rootComponent.childById(ButtonWidget.class, "previous-button")
                .onPress(button -> this.client.setScreen(this.parent));

        rootComponent.childById(ButtonWidget.class, "next-button")
                .onPress(button -> this.close());
    }
}
