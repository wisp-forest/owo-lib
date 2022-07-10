package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HoverContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Map;

public class TestParseScreen extends BaseUIModelScreen<FlowLayout> {

    public TestParseScreen() {
        super(FlowLayout.class, DataSource.file("epic_ui.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
//        var panel = rootComponent.childById(VerticalFlowLayout.class, "config-panel");
//        long now = System.nanoTime();
//
//        for (int i = 1; i <= 25; i++) {
//            panel.child(i % 2 == 0
//                    ? this.createTextOption(i)
//                    : this.createRangeOption(i)
//            );
//        }
//
//        long diff = System.nanoTime() - now;
//        System.out.printf("Config screen built in %.3fms\n", diff / 1000000f);

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

        var growAnimation = rootComponent.childById(LabelComponent.class, "grow-label")
                .margins().animate(250, Easing.SINE, Insets.of(15));
        //noinspection unchecked
        rootComponent.childById(HoverContainer.class, "grow-label-hover")
                .onMouseEnter(o -> growAnimation.forwards())
                .onMouseLeave(o -> growAnimation.backwards());
    }

    protected Component createTextOption(final int index) {
        var option = this.model.expandTemplate(FlowLayout.class,
                "text-config-option",
                Map.of(
                        "config-option-name", "very epic option #" + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueBox = option.childById(TextFieldWidget.class, "value-box");
        option.childById(ButtonWidget.class, "reset-button").onPress(button -> {
            valueBox.setText(String.valueOf(index * index));
        });

        return option;
    }

    protected Component createRangeOption(final int index) {
        var option = this.model.expandTemplate(FlowLayout.class,
                "range-config-option",
                Map.of(
                        "config-option-name", "very epic option #" + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueSlider = option.childById(SliderComponent.class, "value-slider");
        valueSlider.value((index * index) / 625d);

        option.childById(ButtonWidget.class, "reset-button").onPress(button -> {
            valueSlider.value((index * index) / 625d);
        });

        return option;
    }
}