package io.wispforest.academy.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class MarginsTutorialScreen extends BaseUIModelScreen<FlowLayout> {

    public MarginsTutorialScreen() {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo-ui-academy", "margins")));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(ButtonWidget.class, "inspector-button")
                .onPress(button -> this.uiAdapter.toggleInspector());

        rootComponent.childById(ButtonWidget.class, "next-button")
                .onPress(button -> this.client.setScreen(new PaddingTutorialScreen(this)));

        var boxContainer = rootComponent.childById(FlowLayout.class, "box-container");
        var centeredCheckbox = rootComponent.childById(CheckboxComponent.class, "centered-checkbox");
        centeredCheckbox.tooltip(Text.literal("Whether to align the box to the center of the playground"));
        centeredCheckbox.onChanged(center -> {
            boxContainer.verticalAlignment(center ? VerticalAlignment.CENTER : VerticalAlignment.TOP);
            boxContainer.horizontalAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        });

        var theBox = rootComponent.childById(BoxComponent.class, "the-box");

        var topMargin = rootComponent.childById(TextFieldWidget.class, "top-margin-text-box");
        topMargin.setTextPredicate(s -> s.matches("-?\\d*"));
        topMargin.setChangedListener(marginListener(theBox, Insets::withTop));

        var bottomMargin = rootComponent.childById(TextFieldWidget.class, "bottom-margin-text-box");
        bottomMargin.setTextPredicate(s -> s.matches("-?\\d*"));
        bottomMargin.setChangedListener(marginListener(theBox, Insets::withBottom));

        var leftMargin = rootComponent.childById(TextFieldWidget.class, "left-margin-text-box");
        leftMargin.setTextPredicate(s -> s.matches("-?\\d*"));
        leftMargin.setChangedListener(marginListener(theBox, Insets::withLeft));

        var rightMargin = rootComponent.childById(TextFieldWidget.class, "right-margin-text-box");
        rightMargin.setTextPredicate(s -> s.matches("-?\\d*"));
        rightMargin.setChangedListener(marginListener(theBox, Insets::withRight));
    }

    private static Consumer<String> marginListener(Component component, BiFunction<Insets, Integer, Insets> transformer) {
        return s -> {
            if (s.isEmpty() || s.equals("-")) return;
            component.margins(transformer.apply(component.margins().get(), Integer.parseInt(s)));
        };
    }
}
