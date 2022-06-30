package io.wispforest.uwu.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.BaseOwoScreen;
import io.wispforest.owo.ui.OwoUIAdapter;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.definitions.*;
import io.wispforest.owo.ui.layout.FlowLayout;
import io.wispforest.owo.ui.layout.Layouts;
import io.wispforest.owo.ui.layout.ScrollContainer;
import io.wispforest.owo.ui.layout.VerticalFlowLayout;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class TestConfigScreen extends BaseOwoScreen<VerticalFlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<VerticalFlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Layouts::verticalFlow);
    }

    @Override
    protected void build(VerticalFlowLayout rootComponent) {
        rootComponent.surface(Surface.flat(0x77000000))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        var options = IntStream.rangeClosed(1, 25)
                .mapToObj(value -> new ConfigOption("very epic option #" + value, String.valueOf(value * value)))
                .toList();

        rootComponent.child(Components.label(
                Text.literal("very epic ").append(Owo.PREFIX).append("config")
        ).shadow(true).margins(Insets.bottom(15)));

        final var optionsScrollContainer = ScrollContainer.vertical(
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
        var container = Layouts.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        container.padding(Insets.of(5));

        container.child(Components.label(Text.literal(option.name)).positioning(Positioning.relative(0, 50)));

        {
            var valueLayout = Layouts.horizontalFlow(Sizing.content(), Sizing.fill(100));
            valueLayout.positioning(Positioning.relative(100, 50)).verticalAlignment(VerticalAlignment.CENTER);
            container.child(valueLayout);

            valueLayout.child(Components.slider(Sizing.fixed(200), Text.literal("slider for " + option.name)));

            final var valueBox = Components.textBox(Sizing.fixed(80), option.value);
            valueLayout.child(valueBox.margins(Insets.horizontal(5)));

            valueLayout.child(Components.button(Text.literal("⇄"), button -> {
                valueBox.setText(option.value);
            }).margins(Insets.right(5)));
        }

        return container;
    }

    private record ConfigOption(String name, String value) {}

}
