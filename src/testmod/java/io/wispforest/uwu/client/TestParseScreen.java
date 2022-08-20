package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;

public class TestParseScreen extends BaseUIModelScreen<FlowLayout> {

    public TestParseScreen() {
        super(FlowLayout.class, DataSource.file("epic_ui.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var allay = rootComponent.childById(EntityComponent.class, "allay");
        var verticalAnimation = allay.verticalSizing().animate(450, Easing.CUBIC, Sizing.fixed(200));
        var horizontalAnimation = allay.horizontalSizing().animate(450, Easing.CUBIC, Sizing.fixed(200));

        rootComponent.childById(ButtonWidget.class, "allay-button").onPress(button -> {
            verticalAnimation.reverse();
            horizontalAnimation.reverse();
            button.setMessage(Text.of(button.getMessage().getString().equals("+") ? "-" : "+"));
        });

        var stretchAnimation = rootComponent.childById(ItemComponent.class, "stretch-item")
                .verticalSizing().animate(500, Easing.CUBIC, Sizing.fixed(300));
        rootComponent.childById(ButtonWidget.class, "stretch-button").onPress(button -> stretchAnimation.reverse());

        var flyAnimation = rootComponent.childById(ScrollContainer.class, "fly")
                .positioning().animate(350, Easing.QUADRATIC, Positioning.relative(85, 35));
        rootComponent.childById(ButtonWidget.class, "fly-button").onPress(button -> flyAnimation.reverse());

        var growLabel = rootComponent.childById(LabelComponent.class, "grow-label");
        var growAnimation = growLabel.margins().animate(250, Easing.SINE, Insets.of(15));
        growLabel.mouseEnter().subscribe(growAnimation::forwards);
        growLabel.mouseLeave().subscribe(growAnimation::backwards);
    }
}