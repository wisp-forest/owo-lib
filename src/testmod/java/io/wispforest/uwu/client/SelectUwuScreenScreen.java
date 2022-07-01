package io.wispforest.uwu.client;

import io.wispforest.owo.ui.BaseOwoScreen;
import io.wispforest.owo.ui.OwoUIAdapter;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.definitions.*;
import io.wispforest.owo.ui.layout.Layouts;
import io.wispforest.owo.ui.layout.VerticalFlowLayout;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SelectUwuScreenScreen extends BaseOwoScreen<VerticalFlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<VerticalFlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Layouts::verticalFlow);
    }

    @Override
    protected void build(VerticalFlowLayout rootComponent) {
        this.uiAdapter.rootComponent.surface(Surface.flat(0x77000000))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        this.uiAdapter.rootComponent.child(
                Components.label(Text.literal("Available screens"))
                        .shadow(true)
                        .margins(Insets.bottom(5))
        );

        var panel = Layouts.verticalFlow(Sizing.content(), Sizing.content());
        panel.padding(Insets.of(5))
                .surface(Surface.PANEL)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        panel.child(Components.button(Text.literal("code demo"), button -> this.client.setScreen(new ComponentTestScreen())).margins(Insets.vertical(3)));
        panel.child(Components.button(Text.literal("xml demo"), button -> this.client.setScreen(new TestParseScreen())).margins(Insets.vertical(3)));
        panel.child(Components.button(Text.literal("code config"), button -> this.client.setScreen(new TestConfigScreen())).margins(Insets.vertical(3)));
        panel.child(Components.button(Text.literal("xml config"), button -> this.client.setScreen(new UwuConfigScreen())).margins(Insets.vertical(3)));

        this.uiAdapter.rootComponent.child(panel);
    }
}
