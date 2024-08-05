package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.NotNull;

public class SizingTestScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.child(Containers.stack(Sizing.content(), Sizing.content()).<StackLayout>configure(container -> {
            container.horizontalAlignment(HorizontalAlignment.CENTER).surface(Surface.panelWithInset(6)).padding(Insets.of(15));

            var animation = container.horizontalSizing().animate(500, Easing.CUBIC, Sizing.fill(75));
            container.child(Components.button(Text.literal("initialize sizenite"), button -> {
                animation.reverse();
            }).horizontalSizing(Sizing.fill(50)));
        }));

        rootComponent.child(Components.label(Text.literal("bruh").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://wispforest.io")))));
    }
}
