package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class NestedComponentTesting extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
                Components.dropdown(Sizing.content())
                        .button(Text.literal("Option 1"), button -> {
                            // Handle button click event
                        })
                        .checkbox(Text.literal("Option 2"), false, ignored -> {})
                        .nested(Text.literal("Submenu"), Sizing.content(), submenu -> {
                            submenu.id("GO FUCK YOURSELF");
                            submenu.button(Text.literal("Submenu Option"), button -> {
                                // Handle submenu button click event
                            });
                        })
                        .closeWhenNotHovered(false)
                        .padding(Insets.of(5))
                        .surface(Surface.TOOLTIP)
                        .id("funny-id")
        );

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .padding(Insets.of(30))
                .sizing(Sizing.fill(100));
    }
}
