package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.items.UwuItems;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class TooManyComponentsScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
                UIContainers.verticalScroll(
                        Sizing.fill(45), Sizing.fill(45),
                        UIContainers.verticalFlow(Sizing.content(), Sizing.content()).<FlowLayout>configure(flowLayout -> {
                            for (int i = 0; i < 50000; i++) {
                                flowLayout.child(
                                        UIContainers.collapsible(Sizing.content(), Sizing.content(), Component.nullToEmpty(String.valueOf(ThreadLocalRandom.current().nextInt(100000))), false)
                                                .child(
                                                        UIComponents.item(UwuItems.SCREEN_SHARD.getDefaultInstance()).sizing(Sizing.fixed(100))
                                                )
                                );
                            }
                        })
                ).surface(Surface.DARK_PANEL).padding(Insets.of(5))
        ).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
    }
}
