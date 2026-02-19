package io.wispforest.uwu.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public class TestConfigScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.flat(0x77000000))
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);

        var options = IntStream.rangeClosed(1, 25)
            .mapToObj(value -> new ConfigOption("very epic option #" + value, String.valueOf(value * value)))
            .toList();

        rootComponent.child(UIComponents.label(
            Component.literal("very epic ").append(Owo.PREFIX).append("config")
        ).shadow(true).margins(Insets.bottom(15)));

        final var optionsScrollContainer = UIContainers.verticalScroll(
            Sizing.fill(90),
            Sizing.fill(85),
            UIComponents.list(
                options,
                flowLayout -> {},
                this::createOptionComponent,
                true
            )
        );

        rootComponent.child(optionsScrollContainer
            .scrollbarThiccness(4)
            .padding(Insets.of(1))
            .surface(Surface.flat(0x77000000).and(Surface.outline(0xFF121212)))
        );
    }

    private FlowLayout createOptionComponent(ConfigOption option) {
        var container = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        container.padding(Insets.of(5));

        container.child(UIComponents.label(
            Component.literal(option.name)
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal("text momente"))))
        ).positioning(Positioning.relative(0, 50)));

        {
            var valueLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.fill(100));
            valueLayout.positioning(Positioning.relative(100, 50)).verticalAlignment(VerticalAlignment.CENTER);
            container.child(valueLayout);

            valueLayout.child(UIComponents.slider(Sizing.fixed(200)).message(s -> Component.literal("slider for " + option.name)));

            final var valueBox = UIComponents.textBox(Sizing.fixed(80), option.value);
            valueLayout.child(valueBox.margins(Insets.horizontal(5)));

            valueLayout.child(UIComponents.button(Component.literal("⇄"), (ButtonComponent button) -> {
                valueBox.setValue(option.value);
            }).margins(Insets.right(5)));
        }

        return container;
    }

    private record ConfigOption(String name, String value) {}

}
