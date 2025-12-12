package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.client.braid.TestSelector;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectUwuScreenScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.uiAdapter.rootComponent.surface(Surface.flat(0x77000000))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        this.uiAdapter.rootComponent.child(
                UIComponents.label(Component.literal("Available screens"))
                        .shadow(true)
                        .margins(Insets.bottom(5))
        );

        var panel = UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).<FlowLayout>configure(layout -> {
            layout.gap(6)
                    .padding(Insets.of(5))
                    .surface(Surface.PANEL);
        });

        var leftColumn = UIContainers.verticalFlow(Sizing.content(), Sizing.content()).gap(6);
        var rightColumn = UIContainers.verticalFlow(Sizing.content(), Sizing.content()).gap(6);

        panel.children(List.of(leftColumn, rightColumn));

        leftColumn.child(UIComponents.button(Component.literal("code demo"), button -> this.minecraft.setScreen(new ComponentTestScreen())));
        leftColumn.child(UIComponents.button(Component.literal("xml demo"), button -> this.minecraft.setScreen(new TestParseScreen())));
        leftColumn.child(UIComponents.button(Component.literal("code config"), button -> this.minecraft.setScreen(new TestConfigScreen())));
        leftColumn.child(UIComponents.button(Component.literal("xml config"), button -> this.minecraft.setScreen(ConfigScreen.create(Uwu.CONFIG, null))));
        leftColumn.child(UIComponents.button(Component.literal("optimization test"), button -> this.minecraft.setScreen(new TooManyComponentsScreen())));
        leftColumn.child(UIComponents.button(Component.literal("focus cycle test"), button -> this.minecraft.setScreen(new BaseUIModelScreen<>(FlowLayout.class, Identifier.fromNamespaceAndPath("uwu", "focus_cycle_test")) {
            @Override
            protected void build(FlowLayout rootComponent) {}
        })));
        leftColumn.child(UIComponents.button(Component.literal("expand gap test"), button -> this.minecraft.setScreen(new BaseUIModelScreen<>(FlowLayout.class, Identifier.fromNamespaceAndPath("uwu", "expand_gap_test")) {
            @Override
            protected void build(FlowLayout rootComponent) {}
        })));
        rightColumn.child(UIComponents.button(Component.literal("smolnite"), button -> this.minecraft.setScreen(new SmolComponentTestScreen())));
        rightColumn.child(UIComponents.button(Component.literal("sizenite"), button -> this.minecraft.setScreen(new SizingTestScreen())));
        rightColumn.child(UIComponents.button(Component.literal("parse fail"), button -> this.minecraft.setScreen(new ParseFailScreen())));
        rightColumn.child(UIComponents.button(Component.literal("braid"), button -> {
            var settings = new BraidScreen.Settings();
            settings.shouldPause = false;

            this.minecraft.setScreen(new BraidScreen(settings, new TestSelector()));
        }));
        panel.child(UIComponents.button(Component.literal("smolnite"), button -> this.minecraft.setScreen(new SmolComponentTestScreen())));
        panel.child(UIComponents.button(Component.literal("sizenite"), button -> this.minecraft.setScreen(new SizingTestScreen())));
        panel.child(UIComponents.button(Component.literal("parse fail"), button -> this.minecraft.setScreen(new ParseFailScreen())));
        panel.child(UIComponents.button(Component.literal("scissor test"), button -> this.minecraft.setScreen(new ScissorTestScreen())));

        this.uiAdapter.rootComponent.child(panel);
    }
}
