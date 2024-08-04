package io.wispforest.uwu.client;

import OwoUIAdapter;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.items.UwuItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class TooManyComponentsScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
                Containers.verticalScroll(
                        Sizing.fill(45), Sizing.fill(45),
                        Containers.verticalFlow(Sizing.content(), Sizing.content()).<FlowLayout>configure(flowLayout -> {
                            for (int i = 0; i < 50000; i++) {
                                flowLayout.child(
                                        Containers.collapsible(Sizing.content(), Sizing.content(), Text.of(String.valueOf(ThreadLocalRandom.current().nextInt(100000))), false)
                                                .child(
                                                        Components.item(UwuItems.SCREEN_SHARD.getDefaultStack()).sizing(Sizing.fixed(100))
                                                )
                                );
                            }
                        })
                ).surface(Surface.DARK_PANEL).padding(Insets.of(5))
        ).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
    }
}
