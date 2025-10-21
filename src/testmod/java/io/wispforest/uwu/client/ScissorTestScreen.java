package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScissorTestScreen extends BaseOwoScreen<StackLayout> {
    @Override
    protected @NotNull OwoUIAdapter<StackLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::stack);
    }

    @Override
    protected void build(StackLayout rootComponent) {
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        rootComponent.child(Containers.verticalScroll(
            Sizing.fixed(100), Sizing.fixed(35),
            Containers.verticalFlow(Sizing.content(), Sizing.content()).children(List.of(
                Components.box(Sizing.fixed(75), Sizing.fixed(25)),
                Components.textBox(Sizing.fill(100)),
                Components.box(Sizing.fixed(75), Sizing.fixed(25))
//                Components.textBox(Sizing.fill(100)),
//                Components.box(Sizing.fixed(75), Sizing.fixed(25))
            ))
        ).surface(Surface.VANILLA_TRANSLUCENT));
    }
}
