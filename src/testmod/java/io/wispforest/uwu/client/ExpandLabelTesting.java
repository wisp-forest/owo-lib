package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ExpandLabelTesting extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
                Containers.verticalFlow(Sizing.fill(), Sizing.fill())
                        .child(
                                Containers.verticalFlow(Sizing.fill(), Sizing.fill())
                                        .child(
                                                Containers.verticalScroll(Sizing.fill(), Sizing.fixed(100),
                                                        Containers.verticalFlow(Sizing.fill(), Sizing.content())
                                                                .child(
                                                                        Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(60))
                                                                                .child(
                                                                                        Components.label(Text.literal("Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Long Text"))
                                                                                                .horizontalSizing(Sizing.expand())
                                                                                ).child(
                                                                                        Components.textBox(Sizing.fixed(120), "Weeeeeee")
                                                                                                .margins(Insets.left(20))
                                                                                )
                                                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                                )
                                                                .child(
                                                                        Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(100))
                                                                                .child(
                                                                                        Components.label(Text.literal("Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Long Text"))
                                                                                                .horizontalSizing(Sizing.expand())
                                                                                ).child(
                                                                                        Components.textBox(Sizing.fixed(120), "Weeeeeee")
                                                                                                .margins(Insets.left(20))
                                                                                )
                                                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                                )
                                                                .child(
                                                                        Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(100))
                                                                                .child(
                                                                                        Components.label(Text.literal("Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Long Text"))
                                                                                                .horizontalSizing(Sizing.expand())
                                                                                ).child(
                                                                                        Components.textBox(Sizing.fixed(120), "Weeeeeee")
                                                                                                .margins(Insets.left(20))
                                                                                )
                                                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                                )
                                                                .child(
                                                                        Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(100))
                                                                                .child(
                                                                                        Components.label(Text.literal("Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Long Text"))
                                                                                                .horizontalSizing(Sizing.expand())
                                                                                ).child(
                                                                                        Components.textBox(Sizing.fixed(120), "Weeeeeee")
                                                                                                .margins(Insets.left(20))
                                                                                )
                                                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                                )
                                                                .child(
                                                                        Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(100))
                                                                                .child(
                                                                                        Components.label(Text.literal("Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Long Text"))
                                                                                                .horizontalSizing(Sizing.expand())
                                                                                ).child(
                                                                                        Components.textBox(Sizing.fixed(120), "Weeeeeee")
                                                                                                .margins(Insets.left(20))
                                                                                )
                                                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                                )
                                                                .child(
                                                                        Containers.horizontalFlow(Sizing.expand(100), Sizing.fixed(100))
                                                                                .child(
                                                                                        Components.label(Text.literal("Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Really Long Text"))
                                                                                                .horizontalSizing(Sizing.expand())
                                                                                ).child(
                                                                                        Components.textBox(Sizing.fixed(120), "Weeeeeee")
                                                                                                .margins(Insets.left(20))
                                                                                )
                                                                                .verticalAlignment(VerticalAlignment.CENTER)
                                                                )
                                                ).padding(Insets.horizontal(5))

                                        )
                                        .margins(Insets.of(30))
                        )
                        .padding(Insets.of(50))
        )/*.sizing(Sizing.fill())*/;
    }
}
