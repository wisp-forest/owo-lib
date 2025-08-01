package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public class SimpleButtonScreen extends BaseOwoScreen<FlowLayout> {

    private final Consumer<FlowLayout> buttonAdditions;
    private final Text message;

    public SimpleButtonScreen(String title, String message, Map<String, Runnable> buttonAdditions) {
        this(Text.translatable(title), Text.translatable(message), layout -> buttonAdditions.forEach((text, action) -> layout.child(button(Text.translatable(text), btn -> action.run()))));
    }

    public SimpleButtonScreen(Text title, Text message, Map<Text, Consumer<ButtonComponent>> buttonAdditions) {
        this(title, message, layout -> buttonAdditions.forEach((text, action) -> layout.child(button(text, action))));
    }

    private SimpleButtonScreen(Text title, Text message, Consumer<FlowLayout> buttonAdditions) {
        super(title);

        this.message = message;
        this.buttonAdditions = buttonAdditions;
    }

    private static Component button(Text message, Consumer<ButtonComponent> onPress) {
        return Components.button(message, onPress)
            .horizontalSizing(Sizing.fixed(120));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                    Components.label(this.getTitle())
                        .shadow(true)
                        .margins(Insets.top(8))
                )
                .child(
                    Components.label(message)
                        .shadow(true)
                        .margins(Insets.top(6))
                )
                .child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .configure((FlowLayout layout) -> {
                            buttonAdditions.accept(layout);

                            layout.margins(Insets.top(16));
                        })
                        .gap(4)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                )
                .padding(Insets.of(12))
                /*.surface(
                    Surface.VANILLA_TRANSLUCENT.and(Surface.outline(Color.ofRgb(0x3955e5).argb()))
                )*/
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        );

        rootComponent
            .surface(Surface.OPTIONS_BACKGROUND)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);
    }
}
