package io.wispforest.uwu.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public class TestConfigScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.flat(0x77000000))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        var options = IntStream.rangeClosed(1, 25)
                .mapToObj(value -> new ConfigOption("very epic option #" + value, String.valueOf(value * value)))
                .toList();

        rootComponent.child(Components.label(
                Text.literal("very epic ").append(Owo.PREFIX).append("config")
        ).shadow(true).margins(Insets.bottom(15)));

        final var optionsScrollContainer = Containers.verticalScroll(
                Sizing.fill(90),
                Sizing.fill(85),
                Components.list(
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
        var container = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        container.padding(Insets.of(5));

        container.child(Components.label(Text.literal(option.name)).positioning(Positioning.relative(0, 50)));

        {
            var valueLayout = Containers.horizontalFlow(Sizing.content(), Sizing.fill(100));
            valueLayout.positioning(Positioning.relative(100, 50)).verticalAlignment(VerticalAlignment.CENTER);
            container.child(valueLayout);

            valueLayout.child(Components.slider(Sizing.fixed(200)).message(s -> Text.literal("slider for " + option.name)));

            final var valueBox = Components.textBox(Sizing.fixed(80), option.value);
            valueLayout.child(valueBox.margins(Insets.horizontal(5)));

            valueLayout.child(Components.button(Text.literal("⇄"), (ButtonComponent button) -> {
                valueBox.setText(option.value);
            }).margins(Insets.right(5)));
        }

        return container;
    }

    private record ConfigOption(String name, String value) {}

}
