package io.wispforest.academy.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class PaddingTutorialScreen extends BaseUIModelScreen<FlowLayout> {

    private final Screen parent;

    public PaddingTutorialScreen(@Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo-ui-academy", "padding")));
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
                .onPress(button -> this.client.setScreen(new PositioningTutorialScreen(this)));

        var boxContainer = rootComponent.childById(FlowLayout.class, "box-container");
        var bottomRightCheckbox = rootComponent.childById(CheckboxComponent.class, "bottom-right-checkbox");
        bottomRightCheckbox.tooltip(Text.literal("Whether to align the box to the bottom right corner of the playground"));
        bottomRightCheckbox.onChanged(bottomRight -> {
            boxContainer.verticalAlignment(bottomRight ? VerticalAlignment.BOTTOM : VerticalAlignment.TOP);
            boxContainer.horizontalAlignment(bottomRight ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
        });

        var topPadding = rootComponent.childById(TextFieldWidget.class, "top-padding-text-box");
        topPadding.setTextPredicate(s -> s.matches("-?\\d*"));
        topPadding.setChangedListener(paddingListener(boxContainer, Insets::withTop));

        var bottomPadding = rootComponent.childById(TextFieldWidget.class, "bottom-padding-text-box");
        bottomPadding.setTextPredicate(s -> s.matches("-?\\d*"));
        bottomPadding.setChangedListener(paddingListener(boxContainer, Insets::withBottom));

        var leftPadding = rootComponent.childById(TextFieldWidget.class, "left-padding-text-box");
        leftPadding.setTextPredicate(s -> s.matches("-?\\d*"));
        leftPadding.setChangedListener(paddingListener(boxContainer, Insets::withLeft));

        var rightPadding = rootComponent.childById(TextFieldWidget.class, "right-padding-text-box");
        rightPadding.setTextPredicate(s -> s.matches("-?\\d*"));
        rightPadding.setChangedListener(paddingListener(boxContainer, Insets::withRight));
    }

    private static Consumer<String> paddingListener(ParentComponent component, BiFunction<Insets, Integer, Insets> transformer) {
        return s -> {
            if (s.isEmpty() || s.equals("-")) return;
            component.padding(transformer.apply(component.padding().get(), Integer.parseInt(s)));
        };
    }
}
