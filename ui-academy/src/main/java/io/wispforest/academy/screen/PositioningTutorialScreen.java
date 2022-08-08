package io.wispforest.academy.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Positioning;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class PositioningTutorialScreen extends BaseUIModelScreen<FlowLayout> {

    private final Screen parent;

    public PositioningTutorialScreen(@Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo-ui-academy", "positioning")));
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
                .onPress(button -> this.client.setScreen(new AlignmentTutorialScreen(this)));

        // --------

        var theBox = rootComponent.childById(BoxComponent.class, "the-box");
        rootComponent.childById(ButtonWidget.class, "layout-button").onPress(button -> theBox.positioning(Positioning.layout()));

        // --------

        var absoluteButton = rootComponent.childById(ButtonWidget.class, "absolute-button");
        var absoluteX = rootComponent.childById(TextFieldWidget.class, "absolute-x-text-box");
        var absoluteY = rootComponent.childById(TextFieldWidget.class, "absolute-y-text-box");

        absoluteX.setTextPredicate(s -> s.matches("-?\\d*"));
        absoluteX.setChangedListener(positioningListener(absoluteX, absoluteY, absoluteButton));

        absoluteY.setTextPredicate(s -> s.matches("-?\\d*"));
        absoluteY.setChangedListener(positioningListener(absoluteX, absoluteY, absoluteButton));

        absoluteButton.onPress(button -> theBox.positioning(Positioning.absolute(
                Integer.parseInt(absoluteX.getText()),
                Integer.parseInt(absoluteY.getText())
        )));

        // --------

        var relativeButton = rootComponent.childById(ButtonWidget.class, "relative-button");
        var relativeX = rootComponent.childById(TextFieldWidget.class, "relative-x-text-box");
        var relativeY = rootComponent.childById(TextFieldWidget.class, "relative-y-text-box");

        relativeX.setTextPredicate(s -> s.matches("-?\\d*"));
        relativeX.setChangedListener(positioningListener(relativeX, relativeY, relativeButton));

        relativeY.setTextPredicate(s -> s.matches("-?\\d*"));
        relativeY.setChangedListener(positioningListener(relativeX, relativeY, relativeButton));

        relativeButton.onPress(button -> theBox.positioning(Positioning.relative(
                Integer.parseInt(relativeX.getText()),
                Integer.parseInt(relativeY.getText())
        )));
    }

    private static Consumer<String> positioningListener(TextFieldWidget x, TextFieldWidget y, ButtonWidget button) {
        return s -> {
            var xString = x.getText();
            var yString = y.getText();

            button.active = !(xString.isEmpty() || xString.equals("-") || yString.isEmpty() || yString.equals("-"));
        };
    }
}
