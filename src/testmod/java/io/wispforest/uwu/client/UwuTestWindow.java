package io.wispforest.uwu.client;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.window.OwoWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UwuTestWindow extends OwoWindow<FlowLayout> {
    public UwuTestWindow() {
        super(640, 480, "uωu test window!", MinecraftClient.getInstance().getWindow().getHandle());
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.padding(Insets.of(10));

        var inner = Containers.verticalFlow(Sizing.content(), Sizing.content());
        rootComponent.child(Containers.verticalScroll(Sizing.content(), Sizing.fill(100), inner));

        inner.child(Components.label(Text.of("Are you an owl?")));

        var textbox = Components.textBox(Sizing.fixed(60));
        var statusLabel = Components.label(Text.empty());

        textbox.onChanged().subscribe(value -> {
            if (value.equalsIgnoreCase("yes")) {
                statusLabel.text(Text.literal("Owl :)")
                        .formatted(Formatting.GREEN));
            } else {
                statusLabel.text(Text.literal("Not an owl :(")
                        .formatted(Formatting.RED));
            }
        });

        inner
                .child(textbox
                        .margins(Insets.vertical(5)))
                .child(statusLabel);
    }
}
