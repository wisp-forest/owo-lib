package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScissorTestScreen extends BaseOwoScreen<StackLayout> {
    @Override
    protected @NotNull OwoUIAdapter<StackLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::stack);
    }

    @Override
    protected void build(StackLayout rootComponent) {
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        rootComponent.child(UIContainers.verticalScroll(
            Sizing.fixed(100), Sizing.fixed(35),
            UIContainers.verticalFlow(Sizing.content(), Sizing.content()).children(List.of(
                UIComponents.box(Sizing.fixed(75), Sizing.fixed(25)),
                UIComponents.textBox(Sizing.fill(100)),
                UIComponents.box(Sizing.fixed(75), Sizing.fixed(25))
//                Components.textBox(Sizing.fill(100)),
//                Components.box(Sizing.fixed(75), Sizing.fixed(25))
            ))
        ).surface(Surface.VANILLA_TRANSLUCENT));
    }
}
