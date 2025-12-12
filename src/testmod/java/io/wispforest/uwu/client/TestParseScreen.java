package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.network.chat.Component;

public class TestParseScreen extends BaseUIModelScreen<FlowLayout> {

    public TestParseScreen() {
        super(FlowLayout.class, DataSource.file("epic_ui.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
//        rootComponent
//                .surface(Surface.VANILLA_TRANSLUCENT)
//                .horizontalAlignment(HorizontalAlignment.CENTER)
//                .verticalAlignment(VerticalAlignment.CENTER);
//
//        rootComponent.child(Components.button(Text.literal("A Button"), button -> {}));
//
//        rootComponent.child(Containers.verticalFlow(Sizing.content(), Sizing.content())
//                .child(Components.button(Text.literal("A Button"), button -> {}))
//                .padding(Insets.of(10))
//                .surface(Surface.DARK_PANEL)
//                .verticalAlignment(VerticalAlignment.CENTER)
//                .horizontalAlignment(HorizontalAlignment.CENTER)
//        );
//
//        rootComponent.childById(ButtonComponent.class, "the-button").onPress(button -> {
//            System.out.println("click");
//        });

        var allay = rootComponent.childById(EntityComponent.class, "allay");
        var verticalAnimation = allay.verticalSizing().animate(450, Easing.EXPO, Sizing.fixed(200));
        var horizontalAnimation = allay.horizontalSizing().animate(450, Easing.CUBIC, Sizing.fixed(200));

        rootComponent.childById(ButtonComponent.class, "allay-button").onPress(button -> {
            verticalAnimation.reverse();
            horizontalAnimation.reverse();
            button.setMessage(Component.nullToEmpty(button.getMessage().getString().equals("+") ? "-" : "+"));
        });

        rootComponent.childById(TextureComponent.class, "java-logo").visibleArea().animate(
                1000, Easing.SINE, PositionedRectangle.of(0, 0, 128, 16)).forwards();

        var stretchAnimation = rootComponent.childById(ItemComponent.class, "stretch-item")
                .verticalSizing().animate(500, Easing.CUBIC, Sizing.fixed(300));
        rootComponent.childById(ButtonComponent.class, "stretch-button").onPress(button -> stretchAnimation.reverse());

        var flyAnimation = rootComponent.childById(ScrollContainer.class, "fly")
                .positioning().animate(350, Easing.QUADRATIC, Positioning.relative(85, 35));
        rootComponent.childById(ButtonComponent.class, "fly-button").onPress(button -> flyAnimation.reverse());

        var growLabel = rootComponent.childById(LabelComponent.class, "grow-label");
        var growAnimation = growLabel.margins().animate(250, Easing.SINE, Insets.of(15));
        growLabel.mouseEnter().subscribe(growAnimation::forwards);
        growLabel.mouseLeave().subscribe(growAnimation::backwards);
    }
}