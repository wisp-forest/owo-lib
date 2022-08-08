package io.wispforest.academy.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class AlignmentTutorialScreen extends BaseUIModelScreen<FlowLayout> {

    private final Screen parent;

    public AlignmentTutorialScreen(@Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo-ui-academy", "alignment")));
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
                .onPress(button -> this.client.setScreen(new SizingTutorialScreen(this)));

        // --------

        var boxContainer = rootComponent.childById(FlowLayout.class, "box-container");

        rootComponent.childById(ButtonWidget.class, "top-button").onPress(button -> boxContainer.verticalAlignment(VerticalAlignment.TOP));
        rootComponent.childById(ButtonWidget.class, "vertical-center-button").onPress(button -> boxContainer.verticalAlignment(VerticalAlignment.CENTER));
        rootComponent.childById(ButtonWidget.class, "bottom-button").onPress(button -> boxContainer.verticalAlignment(VerticalAlignment.BOTTOM));

        rootComponent.childById(ButtonWidget.class, "left-button").onPress(button -> boxContainer.horizontalAlignment(HorizontalAlignment.LEFT));
        rootComponent.childById(ButtonWidget.class, "horizontal-center-button").onPress(button -> boxContainer.horizontalAlignment(HorizontalAlignment.CENTER));
        rootComponent.childById(ButtonWidget.class, "right-button").onPress(button -> boxContainer.horizontalAlignment(HorizontalAlignment.RIGHT));
    }
}
