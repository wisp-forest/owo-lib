package io.wispforest.academy.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class SizingTutorialScreen extends BaseUIModelScreen<FlowLayout> {

    private final Screen parent;

    public SizingTutorialScreen(@Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier("owo-ui-academy", "sizing")));
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
                .onPress(button -> this.client.setScreen(new LayoutTutorialScreen(this)));

        // --------

        var boxContainer = rootComponent.childById(FlowLayout.class, "box-container");

        // --------

        var contentButton = rootComponent.childById(ButtonWidget.class, "content-button");
        var contentX = rootComponent.childById(TextFieldWidget.class, "content-x-text-box");
        var contentY = rootComponent.childById(TextFieldWidget.class, "content-y-text-box");

        contentX.setTextPredicate(s -> s.matches("\\d*"));
        contentX.setChangedListener(sizingListener(contentX, contentY, contentButton));

        contentY.setTextPredicate(s -> s.matches("\\d*"));
        contentY.setChangedListener(sizingListener(contentX, contentY, contentButton));

        contentButton.onPress(button -> {
            boxContainer.sizing(
                    Sizing.content(Integer.parseInt(contentX.getText())),
                    Sizing.content(Integer.parseInt(contentY.getText()))
            );
        });

        // --------

        var fixedButton = rootComponent.childById(ButtonWidget.class, "fixed-button");
        var fixedX = rootComponent.childById(TextFieldWidget.class, "fixed-x-text-box");
        var fixedY = rootComponent.childById(TextFieldWidget.class, "fixed-y-text-box");

        fixedX.setTextPredicate(s -> s.matches("\\d*"));
        fixedX.setChangedListener(sizingListener(fixedX, fixedY, fixedButton));

        fixedY.setTextPredicate(s -> s.matches("\\d*"));
        fixedY.setChangedListener(sizingListener(fixedX, fixedY, fixedButton));

        fixedButton.onPress(button -> {
            boxContainer.sizing(
                    Sizing.fixed(Integer.parseInt(fixedX.getText())),
                    Sizing.fixed(Integer.parseInt(fixedY.getText()))
            );
        });

        // --------

        var fillButton = rootComponent.childById(ButtonWidget.class, "fill-button");
        var fillX = rootComponent.childById(TextFieldWidget.class, "fill-x-text-box");
        var fillY = rootComponent.childById(TextFieldWidget.class, "fill-y-text-box");

        fillX.setTextPredicate(s -> s.matches("\\d*"));
        fillX.setChangedListener(sizingListener(fillX, fillY, fillButton));

        fillY.setTextPredicate(s -> s.matches("\\d*"));
        fillY.setChangedListener(sizingListener(fillX, fillY, fillButton));

        fillButton.onPress(button -> {
            boxContainer.sizing(
                    Sizing.fill(Integer.parseInt(fillX.getText())),
                    Sizing.fill(Integer.parseInt(fillY.getText()))
            );
        });
    }

    private static Consumer<String> sizingListener(TextFieldWidget x, TextFieldWidget y, ButtonWidget button) {
        return s -> button.active = !(x.getText().isEmpty() || y.getText().isEmpty());
    }
}
